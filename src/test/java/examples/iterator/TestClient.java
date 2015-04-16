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

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.List;

import com.healthmarketscience.rmiio.SerialRemoteIteratorClient;
import com.healthmarketscience.rmiio.SerialRemoteIteratorServer;

/**
 * Example client which sends a bunch of strings (given on the command line)
 * to the example server using a RemoteIterator.
 *
 * @author James Ahlborn
 */
public class TestClient {

  public static void main(String[] args) throws Exception
  {
    if(args.length < 1) {
      System.err.println("Usage: <string1> [<string2> ...]");
      System.exit(1);
    }

    // grab the string arguments from the commandline
    List<String> strings = Arrays.asList(args);

    // get a handle to the remote service to which we want to send the strings
    Registry registry = LocateRegistry.getRegistry(TestServer.REGISTRY_PORT);
    RemoteStringServer stub = (RemoteStringServer)
      registry.lookup("RemoteStringServer");

    System.out.println("Sending " + strings.size()  + " strings");

    SerialRemoteIteratorServer<String> server = null;
    try {
      // setup the remote iterator.  note, the client here is actually acting
      // as an RMI server (very confusing, i know).  this code sets up an RMI
      // server in the client, which the RemoteStringServer will then interact
      // with to get the String data.
      server = new SerialRemoteIteratorServer<String>(strings.iterator());
      SerialRemoteIteratorClient<String> client =
        new SerialRemoteIteratorClient<String>(server); 

      // call the remote method on the server.  the server will actually
      // interact with the RMI "server" we started above to retrieve the
      // String data
      stub.sendStrings(client);
      
    } finally {
      // always make a best attempt to shutdown RemoteIterator
      if(server != null) {
        server.close();
      }
    }

    System.out.println("Finished sending " + strings.size()  + " strings");
    
  }

}
