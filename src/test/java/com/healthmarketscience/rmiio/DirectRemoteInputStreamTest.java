/*
Copyright (c) 2008 Health Market Science, Inc.

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
