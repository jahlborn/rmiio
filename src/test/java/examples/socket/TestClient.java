/*
Copyright (c) 2012 James Ahlborn

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

package examples.socket;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import com.healthmarketscience.rmiio.RmiioUtil;
import com.healthmarketscience.rmiio.socket.RMISocket;

/**
 *
 * @author James Ahlborn
 */
public class TestClient 
{

  public static void main(String[] args) throws Exception {

    // get a handle to the remote service to which we want to send messages
    Registry registry = LocateRegistry.getRegistry(TestServer.REGISTRY_PORT);
    RemoteSocketServer stub = (RemoteSocketServer)
      registry.lookup("RemoteSocketServer");

    RMISocket socket = new RMISocket();
    try {
      socket.setRemoteSource(stub.connect(socket.getSource()));

      byte[] buf = new byte[1024];
      BufferedReader reader = new BufferedReader(
          new InputStreamReader(System.in));
      while(true) {
        
        // read message from standard input
        System.out.println();
        System.out.print("Message to send ['" + TestServer.QUIT_MSG +
                         "' to exit]> ");

        String msg = reader.readLine();

        System.out.println();
        System.out.println("client: Sending message '" + msg + "'");

        // send message to server
        TestServer.sendMessage(socket.getOutputStream(), msg, buf);

        // receive reply from server
        String replyMsg = TestServer.receiveMessage(
            socket.getInputStream(), buf);
        
        System.out.println("client: Got message '" + replyMsg + "'");
        
        // all done if the message to send was 'quit'
        if(TestServer.QUIT_MSG.equals(msg)) {
          break;
        }
      }

    } finally {
      RmiioUtil.closeQuietly(socket);
    }    
  }

}
