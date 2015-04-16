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
package examples.iiop;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Properties;
import javax.naming.Context;
import javax.naming.InitialContext;

import com.healthmarketscience.rmiio.RemoteInputStream;
import com.healthmarketscience.rmiio.RemoteInputStreamClient;
import com.healthmarketscience.rmiio.exporter.IIOPRemoteStreamExporter;
import org.omg.CORBA.ORB;
import org.omg.CORBA.Policy;
import org.omg.PortableServer.ImplicitActivationPolicyValue;
import org.omg.PortableServer.LifespanPolicyValue;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.RequestProcessingPolicyValue;
import org.omg.PortableServer.ServantRetentionPolicyValue;

/**
 * Simple example corba server which can be the target of a streamed file.
 *
 * Follow directions here to run this example:
 * <a href="http://java.sun.com/j2se/1.5.0/docs/guide/rmi-iiop/rmiiiopexample.html">Sun RMI-IIOP Example</a>
 *
 * @author James Ahlborn
 */
public class TestServer implements RemoteFileServer, Remote
{
  private final CorbaExporter _ce;

  public TestServer() throws Exception {
    _ce = new CorbaExporter();
  }

  public void sendFile(RemoteInputStream ristream)
    throws RemoteException, IOException
  {
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

  public static void main(String[] args) throws Exception
  {
    
    TestServer server = new TestServer();    
    Object serverRef = server._ce.export(server, RemoteFileServer.class);

    // bind to registry
    Context initialNamingContext = new InitialContext();
    initialNamingContext.rebind("RemoteFileServer", serverRef);
    
    System.out.println("Server ready");
    server._ce.run();
    
  }

  public static class CorbaExporter extends IIOPRemoteStreamExporter
  {
    private final ORB _orb;
    private final POA _poa;

    public CorbaExporter()
      throws Exception
    {
      Properties p = System.getProperties();
      // add runtime properties here
      p.put("org.omg.CORBA.ORBClass", 
            "com.sun.corba.se.internal.POA.POAORB");
      p.put("org.omg.CORBA.ORBSingletonClass", 
            "com.sun.corba.se.internal.corba.ORBSingleton");
      p.put("java.naming.factory.initial",
            "com.sun.jndi.cosnaming.CNCtxFactory");
      p.put("java.naming.provider.url",
            "iiop://localhost:1060");

      _orb = ORB.init( new String[0], p );

      POA rootPOA = (POA)_orb.resolve_initial_references("RootPOA");

      // STEP 1: Create a POA with the appropriate policies
      Policy[] tpolicy = new Policy[4];
      tpolicy[0] = rootPOA.create_lifespan_policy(
        LifespanPolicyValue.TRANSIENT );
      tpolicy[1] = rootPOA.create_request_processing_policy(
        RequestProcessingPolicyValue.USE_ACTIVE_OBJECT_MAP_ONLY );
      tpolicy[2] = rootPOA.create_servant_retention_policy(
        ServantRetentionPolicyValue.RETAIN);
      tpolicy[3] = rootPOA.create_implicit_activation_policy(
          ImplicitActivationPolicyValue.IMPLICIT_ACTIVATION);
      _poa = rootPOA.create_POA("MyTransientPOA", null, tpolicy);
          
      // STEP 2: Activate the POA Manager, otherwise all calls to the
      // servant hang because, by default, POAManager will be in the 
      // HOLD state.
      _poa.the_POAManager().activate();
    }

    @Override
    public POA getPOA() {
      return _poa;
    }

    public void run() {
      _orb.run();
    }
  }
  
}
