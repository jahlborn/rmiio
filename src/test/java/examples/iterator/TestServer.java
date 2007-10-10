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

package examples.iterator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import com.healthmarketscience.rmiio.RemoteIterator;

/**
 * Example server which consumes a bunch of strings using a RemoteIterator
 * (and sticks them in a local temp file).
 *
 * @author James Ahlborn
 */
public class TestServer {

  public static class StringServer
    implements RemoteStringServer
  {
    public void sendStrings(RemoteIterator<String> iter) throws IOException {
      ObjectOutputStream ostream = null;
      try {

        // create a local temp file in which to dump the strings
        File tempFile = File.createTempFile("sentStrings_", ".dat");
        ostream = new ObjectOutputStream(new FileOutputStream(tempFile));
        System.out.println("Writing file " + tempFile);

        // consume the remote iterator
        while(iter.hasNext()) {
          ostream.writeObject(iter.next());
        }
        ostream.flush();
        
        
      } finally {
        try {
          // always make a best attempt to shutdown RemoteIterator
          if(iter != null) {
            iter.close();
          }
        } finally {
          // close output stream as well
          if(ostream != null) {
            ostream.close();
          }
        }
      }
    }
  }

  public static void main(String[] args) throws Exception
  {
    
    StringServer server = new StringServer();
    RemoteStringServer stub = (RemoteStringServer)
      UnicastRemoteObject.exportObject(server, 0);

    // bind to registry
    Registry registry = LocateRegistry.getRegistry(2013);
    registry.bind("RemoteStringServer", stub);

    System.out.println("Server ready");
    
  }
  
}
