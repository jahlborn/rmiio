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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import com.healthmarketscience.rmiio.RmiioUtil;
import com.healthmarketscience.rmiio.socket.RMISocket;

/**
 *
 * @author James Ahlborn
 */
public class TestServer {

  public static final int REGISTRY_PORT = Registry.REGISTRY_PORT;
  public static final String QUIT_MSG = "quit";
  private static final String CHARSET = "UTF-8";

  public static class SocketServer implements RemoteSocketServer
  {
    public RMISocket.Source connect(RMISocket.Source remoteSource) 
      throws IOException {
      RMISocket socket = new RMISocket(remoteSource);
      RMISocket.Source source = socket.getSource();
      Thread t = new Thread(new EchoHandler(socket));
      t.setDaemon(true);
      t.start();
      return source;
    }
  }

  private static final class EchoHandler implements Runnable
  {
    private final RMISocket _socket;
    private final InputStream _in;
    private final OutputStream _out;

    private EchoHandler(RMISocket socket)
      throws IOException
    {
      _socket = socket;
      _in = socket.getInputStream();
      _out = socket.getOutputStream();
    }

    public void run() {
      byte[] buf = new byte[1024];
      try {
        while(true) {

          String msg = receiveMessage(_in, buf);
          if(msg == null) {
            break;
          }
          
          System.out.println("EchoServer recieved '" + msg + "'");
          sendMessage(_out, "server says: " + msg, buf);

          if(QUIT_MSG.equals(msg)) {
            break;
          }
        }
        
      } catch(IOException e) {
        System.err.println("EchoHandler failed");
        e.printStackTrace(System.err);
      } finally {
        RmiioUtil.closeQuietly(_socket);
      }
      System.out.println("EchoHandler done");
    }
  }
  
  public static void sendMessage(OutputStream out, String msg, byte[] tmpBuf)
    throws IOException
  {
    byte[] msgBytes = msg.getBytes(CHARSET);
    ByteBuffer bb = ByteBuffer.wrap(tmpBuf);
    bb.putInt(msgBytes.length);
    bb.put(msgBytes);
    out.write(tmpBuf, 0, msgBytes.length + 4);
    out.flush();
  }

  public static String receiveMessage(InputStream in, byte[] tmpBuf)
    throws IOException
  {
    int pos = 0;
    int readLen = 4;
    boolean gotMsgLen = false;
    while(true) {
      int numBytes = in.read(tmpBuf, pos, readLen - pos);
      if(numBytes < 0) {
        return null;
      }
      pos += numBytes;
          
      if(readLen == pos) {
        if(!gotMsgLen) {
          readLen = ByteBuffer.wrap(tmpBuf).getInt();
          gotMsgLen = true;
        } else {
          return new String(tmpBuf, 0, readLen, CHARSET);
        }

        pos = 0;
      }
    }
  }

  public static void main(String[] args) throws Exception
  {
    
    SocketServer server = new SocketServer();
    RemoteSocketServer stub = (RemoteSocketServer)
      UnicastRemoteObject.exportObject(server, 0);

    // bind to registry
    Registry registry = LocateRegistry.createRegistry(REGISTRY_PORT);
    registry.bind("RemoteSocketServer", stub);

    System.out.println("Server ready");    
  }
  
}
