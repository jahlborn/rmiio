/*
Copyright (c) 2012 James Ahlborn

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
USA
*/

package examples.socket;

import java.io.IOException;
import java.rmi.Remote;

import com.healthmarketscience.rmiio.socket.RMISocket;

/**
 * A simple Remote interface for an RMI server which enables the creation of a
 * socket-like connection over RMI.
 *
 * @author James Ahlborn
 */
public interface RemoteSocketServer extends Remote
{
  public RMISocket.Source connect(RMISocket.Source remoteSource) 
    throws IOException;
}
