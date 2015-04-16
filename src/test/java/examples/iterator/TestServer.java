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

  public static final int REGISTRY_PORT = Registry.REGISTRY_PORT;

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
    Registry registry = LocateRegistry.createRegistry(REGISTRY_PORT);
    registry.bind("RemoteStringServer", stub);

    System.out.println("Server ready");    
  }
  
}
