/*
Copyright (c) 2007 Health Market Science, Inc.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
USA

You can contact Health Market Science at info@healthmarketscience.com
or at the following address:

Health Market Science
2700 Horizon Drive
Suite 200
King of Prussia, PA 19406
*/

package com.healthmarketscience.rmiio.exporter;

import java.io.IOException;
import java.util.Arrays;

import com.healthmarketscience.rmiio.RemoteInputStreamServer;
import com.healthmarketscience.rmiio.RemoteOutputStreamServer;
import com.healthmarketscience.rmiio.RemoteStreamServer;

/**
 * Helper class for applying a "packaged" RemoteStreamServer invocation to the
 * relevant server instance.  This class and the two client proxy classes may
 * be useful for exporting remote stream servers in alternative RPC
 * frameworks.
 *
 * @author James Ahlborn
 */
public class RemoteStreamServerInvokerHelper
{
  public static final String OUT_USING_COMPRESSION_METHOD = "usingGZIPCompression";
  public static final String OUT_CLOSE_METHOD = "close(boolean)";
  public static final String OUT_FLUSH_METHOD = "flush";
  public static final String OUT_WRITE_PACKET_METHOD = "writePacket(byte[],int)";
  
  public static final String IN_USING_COMPRESSION_METHOD = "usingGZIPCompression";
  public static final String IN_AVAILABLE_METHOD = "available";
  public static final String IN_CLOSE_METHOD = "close(boolean)";
  public static final String IN_READ_PACKET_METHOD = "readPacket(int)";
  public static final String IN_SKIP_METHOD = "skip(long,int)";
  

  private RemoteStreamServerInvokerHelper() {}

  /**
   * Invokes the method with the given name and the given parameters on the
   * given RemoteOutputStreamServer instance, returning the result.
   * @param server the server on which to invoke the method
   * @param methodName the name of the method to invoke, one of the
   *                   {@code OUT_*_METHOD} constants
   * @param parameters parameters for the method invocation (may be
   *                   {@code null} if the method takes no parameters)
   * @return the result of the method call, (or {@code null} for void methods)
   * @throws IOException if the method call throws
   */
  public static Object invoke(RemoteOutputStreamServer server,
                              String methodName, Object[] parameters)
    throws IOException
  {
    if(OUT_WRITE_PACKET_METHOD.equals(methodName)) {
      byte[] packet = (byte[])parameters[0];
      int packetId = (Integer)parameters[1];
      server.writePacket(packet, packetId);
      return null;
    } else if(OUT_USING_COMPRESSION_METHOD.equals(methodName)) {
      return server.usingGZIPCompression();
    } else if(OUT_FLUSH_METHOD.equals(methodName)) {
      server.flush();
      return null;
    } else if(OUT_CLOSE_METHOD.equals(methodName)) {
      boolean success = (Boolean)parameters[0];
      server.close(success);
      return null;
    }

    // invalid method name
    throw new IllegalArgumentException(
        "Unknown invocation on " +
        invocationToString(server, methodName, parameters));
  }
  
  /**
   * Invokes the method with the given name and the given parameters on the
   * given RemoteInputStreamServer instance, returning the result.
   * @param server the server on which to invoke the method
   * @param methodName the name of the method to invoke, one of the
   *                   {@code IN_*_METHOD} constants
   * @param parameters parameters for the method invocation (may be
   *                   {@code null} if the method takes no parameters)
   * @return the result of the method call, (or {@code null} for void methods)
   * @throws IOException if the method call throws
   */
  public static Object invoke(RemoteInputStreamServer server,
                              String methodName, Object[] parameters)
    throws IOException
  {
    if(IN_READ_PACKET_METHOD.equals(methodName)) {
      int packetId = (Integer)parameters[0];
      return server.readPacket(packetId);
    } else if(IN_AVAILABLE_METHOD.equals(methodName)) {
      return server.available();
    } else if(IN_SKIP_METHOD.equals(methodName)) {
      long numBytes = (Long)parameters[0];
      int skipId = (Integer)parameters[1];
      return server.skip(numBytes, skipId);
    } else if(IN_USING_COMPRESSION_METHOD.equals(methodName)) {
      return server.usingGZIPCompression();
    } else if(IN_CLOSE_METHOD.equals(methodName)) {
      boolean success = (Boolean)parameters[0];
      server.close(success);
      return null;
    }

    // invalid method name
    throw new IllegalArgumentException(
        "Unknown invocation on " +
        invocationToString(server, methodName, parameters));
  }

  /**
   * Creates a string with the given server, method name, and parameters.
   */
  private static final String invocationToString(
      RemoteStreamServer<?,?> server, String methodName,
      Object[] parameters)
  {
    return "server " + server + ": " + methodName + " " +
      Arrays.deepToString(parameters);
  }
  
}
