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

package com.healthmarketscience.rmiio.exporter;


import java.io.IOException;

import com.healthmarketscience.rmiio.RemoteOutputStream;

import static com.healthmarketscience.rmiio.exporter.RemoteStreamServerInvokerHelper.*;

/**
 * Base RemoteOutputStream implementation which translates each method
 * invocation into a call to the {@link #invoke} method in a manner compatible
 * with the {@link RemoteStreamServerInvokerHelper} {@code invoke} method for
 * a RemoteOutputStreamServer.  This class may be useful for exporting remote
 * stream servers in alternative RPC frameworks.
 *
 * @author James Ahlborn
 */
public abstract class RemoteOutputStreamClientProxy
  implements RemoteOutputStream
{

  public RemoteOutputStreamClientProxy() {
  }

  public boolean usingGZIPCompression()
    throws IOException
  {
    return (Boolean)invoke(OUT_USING_COMPRESSION_METHOD);
  }
  
  public void close(boolean readSuccess)
    throws IOException
  {
    invoke(OUT_CLOSE_METHOD, readSuccess);
  }

  public void flush()
    throws IOException
  {
    invoke(OUT_FLUSH_METHOD);
  }
  
  public void writePacket(byte[] packet, int packetId)
    throws IOException
  {
    invoke(OUT_WRITE_PACKET_METHOD, packet, packetId);
  }

  /**
   * Invokes the given method name with the given parameters on the remote
   * RemoteInputStreamServer and returns the results.
   * @param methodCode the name of the method to invoke, one of the
   *                   {@code RemoteStreamServerInvokerHelper.IN_*_METHOD}
   *                   constants
   * @param parameters parameters for the method invocation (may be
   *                   {@code null} if the method takes no parameters)
   * @return the result of the method call, (or {@code null} for void methods)
   * @throws IOException if the remote server throws or there is a
   *         communication failure.
   */
  protected abstract Object invoke(int methodCode, Object... parameters)
    throws IOException;
  
}
