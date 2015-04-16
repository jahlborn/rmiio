/*
Copyright (c) 2008 Health Market Science, Inc.

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

package com.healthmarketscience.rmiio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import junit.framework.TestCase;

import static com.healthmarketscience.rmiio.RemoteStreamServerTest.*;

/**
 * @author James Ahlborn
 */
public class DirectRemoteInputStreamTest extends TestCase {

  public DirectRemoteInputStreamTest(String name) {
    super(name);
  }

  public void testDirect() throws Exception
  {
    doTestDirect(false, false);
    doTestDirect(true, false);
  }

  public void testReserialize() throws Exception
  {
    doTestDirect(false, true);
    doTestDirect(true, true);
  }
  
  private void doTestDirect(boolean compress,
                            boolean reserialize) throws Exception
  {
    File testFile = new File(TEST_FILE);

    AccumulateRemoteStreamMonitor<RemoteInputStreamServer> monitor =
      new AccumulateRemoteStreamMonitor<RemoteInputStreamServer>(false);
    
    File tempFile = serializeToTempFile(
        new DirectRemoteInputStream(new FileInputStream(testFile), compress,
                                    monitor));

    assertEquals(FILE_SIZE, monitor._numLocalBytes);
    if(compress) {
      assertTrue(tempFile.length() < testFile.length());
      assertTrue(FILE_SIZE > monitor._numWireBytes);
    } else {
      assertTrue(tempFile.length() > testFile.length());
      assertEquals(FILE_SIZE, monitor._numWireBytes);
    }
    assertTrue(monitor._closed);
    assertTrue(monitor._closedClean);

    RemoteInputStream remoteStream = deserializeFromFile(tempFile);

    if(reserialize) {
      int curWireBytes = monitor._numWireBytes;
      int curLocalBytes = monitor._numLocalBytes;
      
      tempFile = serializeToTempFile(remoteStream);
      remoteStream = deserializeFromFile(tempFile);

      assertEquals(curWireBytes, monitor._numWireBytes);
      assertEquals(curLocalBytes, monitor._numLocalBytes);
    }
    
    InputStream istream = RemoteInputStreamClient.wrap(remoteStream);

    File resultFile = File.createTempFile("serialStream_", ".out");
    resultFile.deleteOnExit();
    
    OutputStream ostream = new FileOutputStream(resultFile);
    copy(istream, ostream, false, false);
    istream.close();
    ostream.close();

    assertTrue(compare(testFile, resultFile, false) == 0);
  }

  public void testNotSerializable() throws Exception
  {
    File testFile = new File(TEST_FILE);

    RemoteInputStream remoteStream =
      new DirectRemoteInputStream(new FileInputStream(testFile), false);
    serializeToTempFile(remoteStream);

    try {
      serializeToTempFile(remoteStream);
      fail("NotSerializableException should have been thrown");
    } catch(NotSerializableException ignored) {
      // success
      assertTrue(ignored.getMessage().contains("SERIAL"));
    }

    remoteStream =
      new DirectRemoteInputStream(new FileInputStream(testFile), false);
    remoteStream.readPacket(0);
    
    try {
      serializeToTempFile(remoteStream);
      fail("NotSerializableException should have been thrown");
    } catch(NotSerializableException ignored) {
      // success
      assertTrue(ignored.getMessage().contains("LOCAL"));
    }
  }
  
  public void testNullStreams() throws Exception
  {
    try {
      new DirectRemoteInputStream(null);
      fail("IllegalArgumentException should have been thrown");
    } catch(IllegalArgumentException e) {
      // success
    }

    try {
      new SimpleRemoteOutputStream(null);
      fail("IllegalArgumentException should have been thrown");
    } catch(IllegalArgumentException e) {
      // success
    }
  }


  private File serializeToTempFile(RemoteInputStream remoteStream)
    throws Exception
  {
    File tempFile = File.createTempFile("serialStream_", ".dat");
    tempFile.deleteOnExit();
    
    ObjectOutputStream oostream = new ObjectOutputStream(
        new FileOutputStream(tempFile));

    oostream.writeObject(remoteStream);
    oostream.close();
    return tempFile;
  }

  private RemoteInputStream deserializeFromFile(File tempFile)
    throws Exception
  {
    ObjectInputStream oistream = new ObjectInputStream(
        new FileInputStream(tempFile));
    RemoteInputStream remoteStream = (RemoteInputStream)oistream.readObject();
    oistream.close();
    return remoteStream;
  }
  
  
}
