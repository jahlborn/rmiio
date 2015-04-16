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

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.ExportException;
import javax.rmi.CORBA.Util;
import javax.rmi.PortableRemoteObject;

import com.healthmarketscience.rmiio.RemoteStreamServer;
import com.healthmarketscience.rmiio.exporter.RemoteStreamExporter;
import org.omg.CORBA.UserException;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;

/**
 * RemoteStreamExporter for use with corba RPC.
 *
 * @author James Ahlborn
 */
public abstract class IIOPRemoteStreamExporter extends RemoteStreamExporter
{

  public IIOPRemoteStreamExporter() {
  }

  @Override
  protected Object exportImpl(RemoteStreamServer<?,?> server)
    throws RemoteException
  {
    return export(server, server.getRemoteClass());
  }

  public Object export(Remote server, Class<?> remoteClass)
    throws RemoteException
  {
    try {
      PortableRemoteObject.exportObject(server);
      POA poa = getPOA();
      Object servant = Util.getTie(server);
      Object ref = poa.servant_to_reference((Servant)servant);
      return PortableRemoteObject.narrow(ref, remoteClass);
    } catch(UserException e) {
      throw new ExportException("could not export corba stream servant", e);
    }
  }
  
  @Override
  protected void unexportImpl(RemoteStreamServer<?,?> server)
    throws Exception
  {
    PortableRemoteObject.unexportObject(server);
  }

  /**
   * @return the POA to use for servant export and activation
   */
  protected abstract POA getPOA();
  
}
