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

import com.healthmarketscience.rmiio.util.InputStreamAdapter;

/**
 * Concrete implementation of a RemoteInputStreamServer which sends
 * uncompressed data, which it will read directly from the underlying
 * InputStream.
 *
 * @see <a href="{@docRoot}/overview-summary.html#Usage_Notes">Usage Notes</a>
 * @see #writeReplace
 *
 * @author James Ahlborn
 */
public class SimpleRemoteInputStream extends RemoteInputStreamServer
{
  private static final long serialVersionUID = 20080212L;  

  /** manages reading from the given stream in a packet-like manner */
  private transient final InputStreamAdapter _inAdapter;
  
  public SimpleRemoteInputStream(InputStream in) {
    this(in, DUMMY_MONITOR, DEFAULT_CHUNK_SIZE);
  }

  public SimpleRemoteInputStream(
    InputStream in,
    RemoteStreamMonitor<RemoteInputStreamServer> monitor) {
    this(in, monitor, DEFAULT_CHUNK_SIZE);
  }

  public SimpleRemoteInputStream(
    InputStream in,
    RemoteStreamMonitor<RemoteInputStreamServer> monitor,
    int chunkSize)
  {
    super(in, monitor, chunkSize);
    _inAdapter = InputStreamAdapter.create(in, _chunkSize);
  }
  
  public boolean usingGZIPCompression()
  {
    // no compression
    return false;
  }

  @Override
  protected int availableImpl()
    throws IOException
  {
    synchronized(getLock()) {
      return _in.available();
    }
  }

  @Override
  protected byte[] readPacket()
    throws IOException
  {
    // will be called synchronized
    
    // read another packet of data
    byte[] packet = _inAdapter.readPacket();
    if(packet != null) {
      _monitor.localBytesMoved(this, packet.length);
    }      
    return packet;
  }

  @Override
  protected long skip(long n)
    throws IOException
  {
    // will be called synchronized
    long numSkipped = _in.skip(n);
    _monitor.localBytesSkipped(this, numSkipped);
    return numSkipped;
  }
  
}
