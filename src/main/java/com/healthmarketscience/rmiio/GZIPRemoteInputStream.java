/*
Copyright (c) 2007 Health Market Science, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.healthmarketscience.rmiio;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPOutputStream;

import com.healthmarketscience.rmiio.util.InputStreamAdapter;
import com.healthmarketscience.rmiio.util.EncodingInputStream;


/**
 * Concrete implementation of a RemoteInputStreamServer which sends compressed
 * data in the GZIP format, which it will read from the underlying
 * InputStream.
 *
 * @see <a href="{@docRoot}/overview-summary.html#Usage_Notes">Usage Notes</a>
 * @see #writeReplace
 *
 * @author James Ahlborn
 */
public class GZIPRemoteInputStream extends RemoteInputStreamServer
{
  private static final long serialVersionUID = 20080212L;  

  /** input stream from which this class retrieves the gzip compressed data
      written to _packetOStream.  The OutputStream linked to this object will
      be used as the sink for the GZIPOutputStream. */
  private transient final GZIPEncodingInputStream _packetIStream;
  /** output stream which is used to compress the underlying data from the
      InputStream */
  private transient GZIPOutputStream _gzipOStream;
  /** manages reading from the underlying stream in a packet-like manner */
  private transient final InputStreamAdapter _inAdapter;
  
  public GZIPRemoteInputStream(InputStream in)
    throws IOException
  {
    this(in, DUMMY_MONITOR, DEFAULT_CHUNK_SIZE);
  }

  public GZIPRemoteInputStream(
    InputStream in,
    RemoteStreamMonitor<RemoteInputStreamServer> monitor)
    throws IOException
  {
    this(in, monitor, DEFAULT_CHUNK_SIZE);
  }

  public GZIPRemoteInputStream(
    InputStream in,
    RemoteStreamMonitor<RemoteInputStreamServer> monitor,
    int chunkSize)
    throws IOException
  {
    super(in, monitor, chunkSize);
    _packetIStream = new GZIPEncodingInputStream(_chunkSize);
    _inAdapter = InputStreamAdapter.create(in, _chunkSize);
  }

  public boolean usingGZIPCompression()
  {
    return true;
  }

  @Override
  protected int availableImpl()
    throws IOException
  {
    synchronized(getLock()) {
      return _packetIStream.available();
    }
  }

  @SuppressWarnings("PMD.UselessOverridingMethod")
  @Override
  protected void closeImpl(boolean readSuccess)
    throws IOException
  {
    // as much as we want to close the _gzipOStream here, we really can't.  if
    // this stream is closing prematurely, closing the _gzipOStream will cause
    // it to generate output, which can never make it over the remote stream
    // anyway (and could cause errors), and, if the stream was read
    // completely, the _gzipOStream will already be closed.  so, we'll just
    // let the stream die a natural death later whenever the garbage collector
    // gets to it
    
    // now close super
    super.closeImpl(readSuccess);
  }

  @Override
  protected byte[] readPacket()
    throws IOException
  {
    // will be called synchronized

    return _packetIStream.readPacket();
  }

  @Override
  protected long skip(long n)
    throws IOException
  {
    throw new IOException("Should not be called on compressed stream");
  }

  /**
   * Utility class which turns gzip output bytes into a sequence of byte[]'s.
   * The OutputStream of this class is used as the sink for data from the
   * GZIPOutputStream.
   */
  private class GZIPEncodingInputStream extends EncodingInputStream
  {
    private GZIPEncodingInputStream(int chunkSize) {
      super(chunkSize);
    }

    @Override
    protected void encode(int suggestedLength)
      throws IOException
    {
      if(_gzipOStream == null) {
        // cannot create GZIPOutputStream until first used (because its
        // constructor generates output!)
        _gzipOStream = new GZIPOutputStream(createOutputStream(),
                                            _chunkSize);
      }
        
      int numRead = _inAdapter.readTemp();
      if(numRead > 0) {

        _monitor.localBytesMoved(GZIPRemoteInputStream.this, numRead);
        
        // push data into the gzipper
        _gzipOStream.write(_inAdapter.getTempBuffer(), 0, numRead);
        
      } else {

        if(_gzipOStream != null) {
          // no more source data, finish it off
          _gzipOStream.close();
        }
      }
    }
  }
  
  
}
