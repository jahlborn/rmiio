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
