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
  public static final int OUT_USING_COMPRESSION_METHOD = 1301; // "usingGZIPCompression";
  public static final int OUT_CLOSE_METHOD = 1302; // "close(boolean)";
  public static final int OUT_FLUSH_METHOD = 1303; // "flush";
  public static final int OUT_WRITE_PACKET_METHOD = 1304; // "writePacket(byte[],int)";
  
  public static final int IN_USING_COMPRESSION_METHOD = 1351; // "usingGZIPCompression";
  public static final int IN_AVAILABLE_METHOD = 1352; // "available";
  public static final int IN_CLOSE_METHOD = 1353; // "close(boolean)";
  public static final int IN_READ_PACKET_METHOD = 1354; // "readPacket(int)";
  public static final int IN_SKIP_METHOD = 1355; // "skip(long,int)";
  

  private RemoteStreamServerInvokerHelper() {}

  /**
   * Invokes the method with the given name and the given parameters on the
   * given RemoteOutputStreamServer instance, returning the result.
   * @param server the server on which to invoke the method
   * @param methodCode the code of the method to invoke, one of the
   *                   {@code OUT_*_METHOD} constants
   * @param parameters parameters for the method invocation (may be
   *                   {@code null} if the method takes no parameters)
   * @return the result of the method call, (or {@code null} for void methods)
   * @throws IOException if the method call throws
   */
  public static Object invoke(RemoteOutputStreamServer server,
                              int methodCode, Object[] parameters)
    throws IOException
  {
    switch(methodCode) {
    case OUT_WRITE_PACKET_METHOD:
      byte[] packet = (byte[])parameters[0];
      int packetId = (Integer)parameters[1];
      server.writePacket(packet, packetId);
      return null;

    case OUT_USING_COMPRESSION_METHOD:
      return server.usingGZIPCompression();

    case OUT_FLUSH_METHOD:
      server.flush();
      return null;

    case OUT_CLOSE_METHOD:
      boolean success = (Boolean)parameters[0];
      server.close(success);
      return null;
    }

    // invalid method code
    throw new IllegalArgumentException(
        "Unknown invocation on " +
        invocationToString(server, methodCode, parameters));
  }
  
  /**
   * Invokes the method with the given name and the given parameters on the
   * given RemoteInputStreamServer instance, returning the result.
   * @param server the server on which to invoke the method
   * @param methodCode the code of the method to invoke, one of the
   *                   {@code IN_*_METHOD} constants
   * @param parameters parameters for the method invocation (may be
   *                   {@code null} if the method takes no parameters)
   * @return the result of the method call, (or {@code null} for void methods)
   * @throws IOException if the method call throws
   */
  public static Object invoke(RemoteInputStreamServer server,
                              int methodCode, Object[] parameters)
    throws IOException
  {
    switch(methodCode) {
    case IN_READ_PACKET_METHOD:
      int packetId = (Integer)parameters[0];
      return server.readPacket(packetId);

    case IN_AVAILABLE_METHOD:
      return server.available();

    case IN_SKIP_METHOD:
      long numBytes = (Long)parameters[0];
      int skipId = (Integer)parameters[1];
      return server.skip(numBytes, skipId);

    case IN_USING_COMPRESSION_METHOD:
      return server.usingGZIPCompression();

    case IN_CLOSE_METHOD:
      boolean success = (Boolean)parameters[0];
      server.close(success);
      return null;
    }

    // invalid method code
    throw new IllegalArgumentException(
        "Unknown invocation on " +
        invocationToString(server, methodCode, parameters));
  }

  /**
   * Creates a string with the given server, method code, and parameters.
   */
  private static final String invocationToString(
      RemoteStreamServer<?,?> server, int methodCode,
      Object[] parameters)
  {
    return "server " + server + ": " + methodCode + " " +
      Arrays.deepToString(parameters);
  }
  
}
