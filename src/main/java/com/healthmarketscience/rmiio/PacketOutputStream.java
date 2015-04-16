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
 * Adds support for packet based access to data from an OutputStream.  Can be
 * more effecient for certain stream implementations, especially for remote
 * stream usage where data is coming across the wire in byte[] packets.
 *
 * @author James Ahlborn
 */
public abstract class PacketOutputStream extends OutputStream
{

  public PacketOutputStream() {
  }

  /**
   * Puts the given "packet" into the output stream.  The packet should be
   * filled with data.  The caller is giving control of the buffer to the
   * PacketOutputStream, and therefore should not attempt to use the byte[]
   * again.
   *
   * @param packet fully filled array of bytes to give to the OutputStream
   */
  public abstract void writePacket(byte[] packet)
    throws IOException;
  
}
