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
