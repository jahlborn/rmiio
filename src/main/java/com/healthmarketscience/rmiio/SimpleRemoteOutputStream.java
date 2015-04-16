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

import com.healthmarketscience.rmiio.util.OutputStreamAdapter;


/**
 * Concrete implementation of a RemoteOutputStreamServer which expects to
 * receive uncompressed data, which it will write directly to the underlying
 * OutputStream.
 *
 * @see <a href="{@docRoot}/overview-summary.html#Usage_Notes">Usage Notes</a>
 * @see #writeReplace
 *
 * @author James Ahlborn
 */
public class SimpleRemoteOutputStream extends RemoteOutputStreamServer 
{
  private static final long serialVersionUID = 20080212L;

  /** manages writing to the given stream in a packet-like manner */
  private transient final OutputStreamAdapter _outAdapter;
  
  public SimpleRemoteOutputStream(OutputStream out) {
    this(out, DUMMY_MONITOR);
  }

  public SimpleRemoteOutputStream(
    OutputStream out,
    RemoteStreamMonitor<RemoteOutputStreamServer> monitor) {
    super(out, monitor);
    _outAdapter = OutputStreamAdapter.create(out);
  }
  
  public boolean usingGZIPCompression()
  {
    // no compression
    return false;
  }

  @Override
  protected void flushImpl()
    throws IOException
  {
    synchronized(getLock()) {
      _out.flush();
    }
  }

  @Override
  protected void writePacket(byte[] packet)
    throws IOException
  {
    // will be called synchronized

    _outAdapter.writePacket(packet);
    _monitor.localBytesMoved(this, packet.length);
  }
  
}
