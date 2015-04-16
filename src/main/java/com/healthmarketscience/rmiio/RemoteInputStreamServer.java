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


/**
 * Server implementation base class for a RemoteInputStream.  Handles the
 * retry logic (the sequence ids) and the RemoteStreamMonitor updates for the
 * server.  Subclasses must implement the actual data handling methods.
 *
 * @see #writeReplace
 * 
 * @author James Ahlborn
 */
public abstract class RemoteInputStreamServer
  extends RemoteStreamServer<RemoteInputStreamServer, RemoteInputStream>
  implements RemoteInputStream
{
  private static final long serialVersionUID = 20080212L;

  /** default chunk size for shuffling data over the wire. */
  public static final int DEFAULT_CHUNK_SIZE = 7 * 1024;

  /** Default monitor for operations done by RemoteInputStreamServer which
      does nothing. */
  public static final RemoteStreamMonitor<RemoteInputStreamServer> DUMMY_MONITOR = new RemoteInputStreamMonitor();

  /** the real input stream from which we are reading data */
  protected transient final InputStream _in;
  /** the target chunk size for data packets sent over the wire */
  protected transient final int _chunkSize;
  /** id of the last packet sent from a readPacket() call */
  private transient int _lastPacketId = INITIAL_INVALID_SEQUENCE_ID;
  /** the last packet sent from readPacket(), corresponds to _lastPacketId */
  private transient byte[] _lastPacket;
  /** id of the last skip call */
  private transient int _lastSkipId = INITIAL_INVALID_SEQUENCE_ID;
  /** the results of the last skip() call, corresponds to _lastSkipId */
  private transient long _lastSkip;

  
  protected RemoteInputStreamServer(InputStream in) {
    this(in, DUMMY_MONITOR, DEFAULT_CHUNK_SIZE);
  }

  protected RemoteInputStreamServer(
    InputStream in,
    RemoteStreamMonitor<RemoteInputStreamServer> monitor)
  {
    this(in, monitor, DEFAULT_CHUNK_SIZE);
  }

  /**
   * @param in the real input stream from which the data will be read
   * @param monitor monitor for tracking the progress of the stream usage
   * @param chunkSize target value for the byte size of the packets of data
   *                  sent over the wire.  note that this is a suggestion,
   *                  actual package sizes may vary.
   */
  public RemoteInputStreamServer(
    InputStream in,
    RemoteStreamMonitor<RemoteInputStreamServer> monitor,
    int chunkSize)
  {
    super(monitor);
    if(in == null) {
      throw new IllegalArgumentException("InputStream cannot be null");
    }
    _in = in;
    _chunkSize = chunkSize;
  }

  /** Returns the real InputStream from which this stream is reading data */
  public InputStream getInputStream() { return _in; }

  @Override
  protected final Object getLock() { return _in; }

  @Override
  protected RemoteInputStreamServer getAsSub() { return this; }

  @Override
  public Class<RemoteInputStream> getRemoteClass() {
    return RemoteInputStream.class;
  }
      
  @Override
  protected void closeImpl(boolean readSuccess)
    throws IOException
  {
    synchronized(getLock()) {
      // close input
      _in.close();
    }
  }
  
  public final void close(boolean readSuccess)
    throws IOException
  {
    // close up underlying stuff
    finish(true, readSuccess);
  }

  public final int available()
    throws IOException
  {
    checkAborted();
    
    return availableImpl();
  }
  
  public final byte[] readPacket(int packetId)
    throws IOException
  {
    if(packetId < 0) {
      throw new IllegalArgumentException("packetId must be >= 0.");
    }

    checkAborted();
    
    synchronized(getLock()) {
      if(packetId < _lastPacketId) {
        throw new IllegalArgumentException("packetId must increase.");
      }

      boolean isReattempt = false;
      if(packetId != _lastPacketId) {

        try {
          _lastPacket = readPacket();
        } catch(IOException e) {
          // update the monitor
          _monitor.failure(this, e);
          throw e;
        } catch(RuntimeException e) {
          // update the monitor
          _monitor.failure(this, e);
          throw e;
        }
        
        // update packetId
        _lastPacketId = packetId;

      } else {
        
        // try again!
        isReattempt = true;
      }

      if(_lastPacket != null) {
        // update the monitor
        _monitor.bytesMoved(this, _lastPacket.length, isReattempt);
      }
      
      return _lastPacket;
    }
  }
  
  public final long skip(long n, int skipId)
    throws IOException
  {
    if(skipId < 0) {
      throw new IllegalArgumentException("skipId must be >= 0.");
    }

    checkAborted();
    
    synchronized(getLock()) {
      if(skipId < _lastSkipId) {
        throw new IllegalArgumentException("skipId must increase.");
      }
      
      boolean isReattempt = false;
      if(skipId != _lastSkipId) {

        try {
          _lastSkip = skip(n);
        } catch(IOException e) {
          // update the monitor
          _monitor.failure(this, e);
          throw e;
        } catch(RuntimeException e) {
          // update the monitor
          _monitor.failure(this, e);
          throw e;
        }

        // update skipId
        _lastSkipId = skipId;
        
      } else {
        
        // try again!
        isReattempt = true;
      }

      // update the monitor (return actual bytes skipped)
      _monitor.bytesSkipped(this, _lastSkip, isReattempt);
      
      return _lastSkip;
    }
  }

  
  /**
   * Returns the number of bytes that can be read from this stream without
   * blocking.
   *
   * @return the number of bytes that can be read without blocking
   */
  protected abstract int availableImpl()
    throws IOException;
  
  /**
   * Reads the next packet of approximately {@link #_chunkSize} from the
   * underlying stream and returns it.  If this stream is using compression,
   * this packet should contain compressed data.
   *
   * @return the next packet of data for this stream
   */
  protected abstract byte[] readPacket()
    throws IOException;

  /**
   * Skips at most the given amount of bytes in the underlying stream and
   * returns the actual number of bytes skipped.
   *
   * @return the actual number of bytes skipped
   */
  protected abstract long skip(long n)
    throws IOException;
  
}
