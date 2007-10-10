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

package com.healthmarketscience.rmiio;

import java.io.IOException;
import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.util.ArrayList;


/**
 * @author James Ahlborn
 */
public class RemoteIteratorTest extends BaseRemoteStreamTest {

  private static final Log LOG = LogFactory.getLog(RemoteIteratorTest.class);


  public void testTransfer() throws Exception
  {
    List<List<TestObject>> testObjLists = mainTest(false, false, false,
                                                   _clientExceptions,
                                                   _monitors);

    checkClientExceptions(0);
    
    // should have 3 lists
    assertEquals(3, testObjLists.size());
    
    // compare all sent objects to originals
    for(List<TestObject> testObjList : testObjLists) {
      assertEquals(ObjectClient.SEND_OBJECTS, testObjList);
    }

    checkMonitors(5, true);
  }

  public void testEmptyTransfer() throws Exception
  {
    List<List<TestObject>> testObjLists = mainTest(true, false, false,
                                                   _clientExceptions,
                                                   _monitors);

    checkClientExceptions(0);
    
    // should have 3 lists
    assertEquals(3, testObjLists.size());
    
    // should have empty result lists
    for(List<TestObject> testObjList : testObjLists) {
      assertTrue(testObjList.isEmpty());
    }

    checkMonitors(5, true);
  }

  public void testAbortedTransfer() throws Exception
  {
    List<List<TestObject>> testObjLists = mainTest(false, true, false,
                                                   _clientExceptions,
                                                   _monitors);

    checkClientExceptions(3);
    
    checkMonitors(3, false);
  }

  public void testNoDelayTransfer() throws Exception
  {
    List<List<TestObject>> testObjLists = mainTest(false, false, true,
                                                   _clientExceptions,
                                                   _monitors);

    // should have one list with one element
    assertEquals(1, testObjLists.size());
    assertEquals(1, testObjLists.get(0).size());

    assertEquals(ObjectClient.SEND_OBJECTS.get(0),
                 testObjLists.get(0).get(0));
    
    checkClientExceptions(1);
    
    checkMonitors(1, false);
  }

  public void testSimple() throws Exception
  {
    List<TestObject> srcList = new ArrayList<TestObject>();
    srcList.addAll(Arrays.asList(
                       new TestObject("obj1", 1),
                       new TestObject("obj2", 2),
                       null,
                       new TestObject("obj3", 3),
                       new TestObject("obj4", 4)));

    RemoteIterator<TestObject> srcIter =
      new SimpleRemoteIterator<TestObject>(srcList);

    // serialize/deserialize
    RemoteIterator<TestObject> dstIter =
      RemoteStreamServerTest.simulateRemote(srcIter);

    assertNotSame(srcIter, dstIter);

    List<TestObject> dstList = new ArrayList<TestObject>();
    while(dstIter.hasNext()) {
      dstList.add(dstIter.next());
    }
    
    assertEquals(srcList, dstList);
  }

  public static List<List<TestObject>> mainTest(
      final boolean sendEmptyList,
      final boolean doAbort,
      final boolean noDelayAbort,
      final List<Throwable> clientExceptions,
      final List<AccumulateRemoteStreamMonitor<?>> monitors)
    throws Exception
  {

    ObjectServer server = new ObjectServer();
    final RemoteObjectServer stub = (RemoteObjectServer)
      RemoteStreamServerTest.simulateRemote(
          UnicastRemoteObject.exportObject(server, 0));
    LOG.debug("Server ready");

    LOG.debug("Sleeping 3000 ms...");
    Thread.sleep(3000);

    LOG.debug("Running tests");
    Thread clientThread = new Thread(new Runnable()
      {
        public void run() {
          clientExceptions.addAll(
              ObjectClient.main(stub, sendEmptyList, doAbort, noDelayAbort,
                                monitors));
        }
      });
    clientThread.start();
    clientThread.join();

    LOG.debug("Unexporting server");
    UnicastRemoteObject.unexportObject(server, true);

    return server._recvdObjectLists;
  }

    
  public interface RemoteObjectServer extends Remote {

    public void sendObjects(RemoteIterator<TestObject> iterator,
                            boolean doPartial)
      throws IOException;

  }

  public static class ObjectServer
    implements RemoteObjectServer
  {

    private List<List<TestObject>> _recvdObjectLists =
      new LinkedList<List<TestObject>>();
  
    public ObjectServer() {
    }

    public void sendObjects(RemoteIterator<TestObject> iterator,
                            boolean doPartial)
      throws IOException
    {
      List<TestObject> recvdObjectList = new LinkedList<TestObject>();
      if(!doPartial) {
        _recvdObjectLists.add(recvdObjectList);
      }
      try {
        int numRecvd = 0;
        while(iterator.hasNext()) {
          recvdObjectList.add(iterator.next());
          ++numRecvd;
          if(doPartial && (numRecvd > ObjectClient.PARTIAL_SIZE)) {
            // just stop reading
            break;
          }
        }
      } finally {
        iterator.close();
      }
      LOG.debug("Server got " + recvdObjectList.size() +
                         " objects");
    }

  }

  public static class ObjectClient
  {

    private static final List<TestObject> SEND_OBJECTS =
      new LinkedList<TestObject>();
    private static final int PARTIAL_SIZE = 100;

    static {
      SEND_OBJECTS.addAll(Arrays.asList(
                            new TestObject("obj1", 1),
                            new TestObject("obj2", 2),
                            null,
                            new TestObject("obj3", 3),
                            new TestObject("obj4", 4)));
      // make this list big!
      SEND_OBJECTS.addAll(Collections.nCopies(10000,
                                              new TestObject("objMany", 13)));
    }

    private ObjectClient() {
    }

    private static void sendObjects(
        RemoteObjectServer stub,
        boolean useCompression,
        boolean doPartial,
        boolean sendEmptyList,
        boolean doAbort,
        boolean noDelay,
        boolean noDelayAbort,
        List<AccumulateRemoteStreamMonitor<?>> monitors)
      throws Exception
    {
      LOG.debug("Getting iterator");
      List<TestObject> sendList = Collections.<TestObject>emptyList();
      if(!sendEmptyList) {
        sendList = SEND_OBJECTS;
      }
      AccumulateRemoteIteratorMonitor<RemoteInputStreamServer> monitor =
        new AccumulateRemoteIteratorMonitor<RemoteInputStreamServer>();
      if(monitors != null) {
        monitors.add(monitor);
      }

      SerialRemoteIteratorServer<TestObject> server = null;
      try {
        server =
          new SerialRemoteIteratorServer<TestObject>(
              useCompression, noDelay, monitor,
              sendList.iterator());
        SerialRemoteIteratorClient<TestObject> client =
          RemoteStreamServerTest.simulateRemote(
              new SerialRemoteIteratorClient<TestObject>(server));

        if(doAbort) {
          monitor.setDoAbort(server, doAbort);
        }
        if(noDelayAbort) {
          monitor.setNoDelayAbort(server, noDelayAbort);
        }

        stub.sendObjects(client, doPartial);
      } finally {
        if(server != null) {
          server.close();
        }
      }
      
      LOG.debug("Sent objects");
    }
  

    public static List<Throwable> main(
        RemoteObjectServer stub,
        boolean sendEmptyList,
        boolean doAbort,
        boolean noDelayAbort,
        List<AccumulateRemoteStreamMonitor<?>> monitors)
    {
      List<Throwable> exceptions = new ArrayList<Throwable>();
      
      // try uncompressed, noDelay send
      try {
        sendObjects(stub, false, false, sendEmptyList, doAbort, true,
                    noDelayAbort, monitors);
      } catch(Throwable t) {
        exceptions.add(t);
      }

      if(!noDelayAbort) {

        // try uncompressed send
        try {
          sendObjects(stub, false, false, sendEmptyList, doAbort, false, false,
                      monitors);
        } catch(Throwable t) {
          exceptions.add(t);
        }
        
        // try compressed send
        try {
          sendObjects(stub, true, false, sendEmptyList, doAbort, false, false,
                      monitors);
        } catch(Throwable t) {
          exceptions.add(t);
        }

        if(!doAbort) {
          
          // try partial uncompressed send
          try {
            sendObjects(stub, false, true, sendEmptyList, false, false, false,
                        monitors);
          } catch(Throwable t) {
            exceptions.add(t);
          }

          // try partial compressed send
          try {
            sendObjects(stub, true, true, sendEmptyList, false, false, false,
                        monitors);
          } catch(Throwable t) {
            exceptions.add(t);
          }
        }
      }
      
      return exceptions;
    }

  }

  private static class AccumulateRemoteIteratorMonitor<S extends RemoteStreamServer>
    extends AccumulateRemoteStreamMonitor<S>
  {
    private RemoteIteratorServer _server;
    private boolean _noDelayAbort;

    AccumulateRemoteIteratorMonitor() {
      super(false);
    }
    
    public void setDoAbort(RemoteIteratorServer server,
                           boolean doAbort) {
      _server = server;
      _doAbort = doAbort;
    }

    public void setNoDelayAbort(RemoteIteratorServer server,
                                boolean noDelayAbort) {
      _server = server;
      _noDelayAbort = noDelayAbort;
    }

    @Override
    public void localBytesMoved(S stream, int numBytes)
    {
      if(_noDelayAbort && (_numLocalBytes > 0)) {
        try {
          _server.abort();
        } catch(IOException e) {
          throw new RuntimeException(e);
        }
      }
      _numLocalBytes += numBytes;
      if(_doAbort && (_numLocalBytes > 0)) {
        try {
          _server.abort();
        } catch(IOException e) {
          throw new RuntimeException(e);
        }
      }
    }

  }
  
  public static class TestObject implements Serializable
  {
    private static final long serialVersionUID = 1;
    
    private String _strData;
    private int _intData;

    public TestObject(String strData, int intData) {
      _strData = strData;
      _intData = intData;
    }

    @Override
    public boolean equals(Object o) {
      return((o instanceof TestObject) &&
             (((TestObject)o)._strData.equals(_strData)) &&
             (((TestObject)o)._intData == _intData));
    }

  }
  
}
