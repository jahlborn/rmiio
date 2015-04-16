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

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import com.healthmarketscience.rmiio.RemoteStreamServer;


/**
 * Default concrete implementation of RemoteStreamExporter which exports the
 * object for use with with standard RMI, via {@link UnicastRemoteObject}.
 * <p>
 * The default constructor will use a port configured by the system property
 * {@link #PORT_PROPERTY} if one is found, otherwise {@link #ANY_PORT} will be
 * used.
 *
 * @author James Ahlborn
 */
public class DefaultRemoteStreamExporter extends RemoteStreamExporter
{
  /** constant indicating that export can use any port */
  public static final int ANY_PORT = 0;

  /** system property used to determine the port to use for the default
      constructor.  if not given, {@link #ANY_PORT} is used. */
  public static final String PORT_PROPERTY =
    "com.healthmarketscience.rmiio.exporter.port";

  /** port number to use when exporting streams */
  private final int _port;
  
  public DefaultRemoteStreamExporter() {
    this(getDefaultPort());
  }

  public DefaultRemoteStreamExporter(int port) {
    _port = port;
  }

  public int getPort() {
    return _port;
  }

  @Override
  protected Object exportImpl(RemoteStreamServer<?,?> server)
    throws RemoteException
  {
    return UnicastRemoteObject.exportObject(server, getPort());
  }
  
  @Override
  protected void unexportImpl(RemoteStreamServer<?,?> server)
    throws Exception
  {
    UnicastRemoteObject.unexportObject(server, true);
  }

  /**
   * Determines the port to use for the default constructor.  If the system
   * property {@link #PORT_PROPERTY} has a valid integer it will be returned,
   * otherwise {@link #ANY_PORT} will be returned.
   * @return a port number
   */
  private static int getDefaultPort()
  {
    return Integer.getInteger(PORT_PROPERTY, ANY_PORT);
  }
  
}
