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

package com.healthmarketscience.rmiio.util;

import java.io.IOException;
import java.io.OutputStream;

import com.healthmarketscience.rmiio.PacketOutputStream;


/**
 * Utility class for optimizing different write strategies based on the type
 * of the underlying OutputStream.
 *
 * @author James Ahlborn
 */
public abstract class OutputStreamAdapter
{

  private OutputStreamAdapter() {
  }

  /**
   * @return the underlying OutputStream
   */
  public abstract OutputStream getOutputStream();

  /**
   * Puts the given "packet" into the output stream.  The packet should be
   * filled with data.  The caller is giving control of the buffer to the
   * PacketOutputStream, and therefore should not attempt to use the byte[]
   * again.
   *
   * @param packet fully filled array of bytes to give to the OutputStream
   */
  public abstract void writePacket(byte[] packet) throws IOException;

  /**
   * @param istream stream to wrap and for which the implementation is
   *                optimized
   * @return an OutputStreamAdapter optimized for the stream type
   */
  public static OutputStreamAdapter create(
      OutputStream istream)
  {
    if(istream instanceof PacketOutputStream) {
      return new PacketAdapter((PacketOutputStream)istream);
    }
    return new DefaultAdapter(istream);
  }

  /**
   * OutputStreamAdapter implementation for PacketOutputStreams.
   */
  private static class PacketAdapter extends OutputStreamAdapter
  {
    private final PacketOutputStream _postream;
    
    private PacketAdapter(PacketOutputStream postream) {
      _postream = postream;
    }

    @Override
    public PacketOutputStream getOutputStream() { return _postream; }
    
    @Override
    public void writePacket(byte[] packet) throws IOException {
      _postream.writePacket(packet);
    }
    
  }

  /**
   * OutputStreamAdapter implementation for normal OutputStreams.
   */
  private static class DefaultAdapter extends OutputStreamAdapter
  {
    private final OutputStream _ostream;
    
    private DefaultAdapter(OutputStream ostream) {
      _ostream = ostream;
    }

    @Override
    public OutputStream getOutputStream() { return _ostream; }
    
    @Override
    public void writePacket(byte[] packet) throws IOException {
      _ostream.write(packet, 0, packet.length);
    }
    
  }
  
  
}
