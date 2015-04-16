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
import java.io.OutputStream;


/**
 * Server implementation base class for a RemoteOutputStream.  Handles the
 * retry logic (the sequence ids) and the RemoteStreamMonitor updates for the
 * server.  Subclasses must implement the actual data handling methods.
 *
 * @see #writeReplace
 * 
 * @author James Ahlborn
 */
public abstract class RemoteOutputStreamServer
  extends RemoteStreamServer<RemoteOutputStreamServer, RemoteOutputStream>
  implements RemoteOutputStream
{
  private static final long serialVersionUID = 20080212L;

  /** Default monitor for operations done by RemoteOutputStreamServer which
      does nothing. */
  public static final RemoteStreamMonitor<RemoteOutputStreamServer> DUMMY_MONITOR = new RemoteOutputStreamMonitor();

  /** the real output stream to which we are writing data */
  protected transient final OutputStream _out;
  /** id of the last packet passed into a writePacket() call */
  private transient int _lastPacketId = INITIAL_INVALID_SEQUENCE_ID;

  public RemoteOutputStreamServer(OutputStream out) {
    this(out, DUMMY_MONITOR);
  }
  
  /**
   * @param out the real output stream from which the data will be read
   * @param monitor monitor for tracking the progress of the stream usage
   */
  public RemoteOutputStreamServer(
    OutputStream out,
    RemoteStreamMonitor<RemoteOutputStreamServer> monitor)
  {
    super(monitor);
    if(out == null) {
      throw new IllegalArgumentException("OutputStream cannot be null");
    }
    _out = out;
  }

  /** Returns the real OutputStream to which this stream is writing data */
  public OutputStream getOutputStream() { return _out; }
  
  @Override
  protected final Object getLock() { return _out; }

  @Override
  protected RemoteOutputStreamServer getAsSub() { return this; }
    
  @Override
  public Class<RemoteOutputStream> getRemoteClass() {
    return RemoteOutputStream.class;
  }

  @Override
  protected void closeImpl(boolean writeSuccess)
    throws IOException
  {
    synchronized(getLock()) {
      // close output
      _out.close();
    }
  }
  
  public final void close(boolean writeSuccess)
    throws IOException
  {
    // close up underlying stuff
    finish(true, writeSuccess);
  }

  public final void flush()
    throws IOException
  {
    checkAborted();

    flushImpl();
  }
  
  public final void writePacket(byte[] packet, int packetId)
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
          writePacket(packet);
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

      // update the monitor
      _monitor.bytesMoved(this, packet.length, isReattempt);
    }
  }

  /**
   * Flushes the underlying stream.
   */
  protected abstract void flushImpl()
    throws IOException;
  
  /**
   * Writes the given packet to the underlying stream.  If this stream is
   * using compression, this packet should contain compressed data.
   */
  protected abstract void writePacket(byte[] packet)
    throws IOException;
  
}
