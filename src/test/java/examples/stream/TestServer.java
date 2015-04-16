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
package examples.stream;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import com.healthmarketscience.rmiio.RemoteInputStream;
import com.healthmarketscience.rmiio.RemoteInputStreamClient;

/**
 * Simple example server which can be the target of a streamed file.
 *
 * @author James Ahlborn
 */
public class TestServer {

  public static final int REGISTRY_PORT = Registry.REGISTRY_PORT;

  public static class FileServer
    implements RemoteFileServer
  {
    public void sendFile(RemoteInputStream ristream) throws IOException {
      InputStream istream = RemoteInputStreamClient.wrap(ristream);
      FileOutputStream ostream = null;
      try {

        File tempFile = File.createTempFile("sentFile_", ".dat");
        ostream = new FileOutputStream(tempFile);
        System.out.println("Writing file " + tempFile);

        byte[] buf = new byte[1024];

        int bytesRead = 0;
        while((bytesRead = istream.read(buf)) >= 0) {
          ostream.write(buf, 0, bytesRead);
        }
        ostream.flush();

        System.out.println("Finished writing file " + tempFile);
        
      } finally {
        try {
          if(istream != null) {
            istream.close();
          }
        } finally {
          if(ostream != null) {
            ostream.close();
          }
        }
      }
    }
  }

  public static void main(String[] args) throws Exception
  {
    
    FileServer server = new FileServer();
    RemoteFileServer stub = (RemoteFileServer)
      UnicastRemoteObject.exportObject(server, 0);

    // bind to registry
    Registry registry = LocateRegistry.createRegistry(REGISTRY_PORT);
    registry.bind("RemoteFileServer", stub);

    System.out.println("Server ready");
    
  }
  
}
