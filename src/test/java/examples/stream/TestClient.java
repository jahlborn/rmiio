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
    Registry registry = LocateRegistry.getRegistry(TestServer.REGISTRY_PORT);
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
