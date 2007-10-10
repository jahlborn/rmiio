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
package examples.stream;

import java.io.FileInputStream;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import com.healthmarketscience.rmiio.SimpleRemoteInputStream;

/**
 * Example client which sends a file (given on the command line) to the
 * example server using a RemoteInputStream.
 *
 * @author James Ahlborn
 */
public class TestClient {

  public static void main(String[] args) throws Exception
  {
    if(args.length < 1) {
      System.err.println("Usage: <file>");
      System.exit(1);
    }

    // grab the file name from the commandline
    String fileName = args[0];
    
    // get a handle to the remote service to which we want to send the file
    Registry registry = LocateRegistry.getRegistry();
    RemoteFileServer stub = (RemoteFileServer)
      registry.lookup("RemoteFileServer");

    System.out.println("Sending file " + fileName);

    // setup the remote input stream.  note, the client here is actually
    // acting as an RMI server (very confusing, i know).  this code sets up an
    // RMI server in the client, which the RemoteFileServer will then
    // interact with to get the file data.
    SimpleRemoteInputStream istream = new SimpleRemoteInputStream(
        new FileInputStream(fileName));
    try {
      // call the remote method on the server.  the server will actually
      // interact with the RMI "server" we started above to retrieve the
      // file data
      stub.sendFile(istream.export());
    } finally {
      // always make a best attempt to shutdown RemoteInputStream
      istream.close();
    }

    System.out.println("Finished sending file " + fileName);
    
  }

}
