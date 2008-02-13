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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import junit.framework.TestCase;

import static com.healthmarketscience.rmiio.RemoteStreamServerTest.*;


/**
 * @author James Ahlborn
 */
public class RemoteStreamMonitorTest extends TestCase {

  public RemoteStreamMonitorTest(String name) {
    super(name);
  }

  public void testInputMonitor() throws Exception
  {
    doTestInputMonitor(false);
    doTestInputMonitor(true);
  }
  
  private void doTestInputMonitor(boolean compress) throws Exception
  {
    AccumulateRemoteStreamMonitor<RemoteInputStreamServer> monitor =
      new AccumulateRemoteStreamMonitor<RemoteInputStreamServer>(false);

    InputStream localInput =
      new BufferedInputStream(new FileInputStream(TEST_FILE));
    RemoteInputStream input =
      (compress ?
       new GZIPRemoteInputStream(localInput, monitor) :
       new SimpleRemoteInputStream(localInput, monitor));

    byte[] packet = null;
    int packetId = 0;
    int numWireBytes = 0;
    int numWirePackets = 0;
    for(int i = 0; i < 5; ++i) {
      packet = input.readPacket(packetId++);
      numWireBytes += packet.length;
      ++numWirePackets;
      if(!compress) {
        assertEquals(monitor._numWireBytes, monitor._numLocalBytes);
      } else {
        assertTrue(monitor._numWireBytes <= monitor._numLocalBytes);
      }
      assertEquals(numWireBytes, monitor._numWireBytes);
      assertEquals(numWirePackets, monitor._numWirePackets);
    }

    // reread packet
    byte[] curPacket = packet;
    int curLocalBytes = monitor._numLocalBytes;
    packet = input.readPacket(packetId - 1);
    assertSame(curPacket, packet);
    assertEquals(curLocalBytes, monitor._numLocalBytes);
    assertEquals(numWireBytes, monitor._numWireBytes);
    assertEquals(numWirePackets, monitor._numWirePackets);
    assertEquals(1, monitor._numReattempts);

    final long skipLen = 2000L;
    long skipped = 0;

    if(!compress) {
      skipped = input.skip(skipLen, packetId++);
      assertTrue((skipped > 0L) && (skipped <= skipLen));
      ++numWirePackets;
      assertEquals(monitor._numSkippedWireBytes,
                   monitor._numSkippedLocalBytes);
      assertEquals(skipped, monitor._numSkippedWireBytes);
      assertEquals(numWirePackets, monitor._numWirePackets);
    
      // redo skip
      long curSkipped = skipped;
      long curLocalSkipped = monitor._numSkippedLocalBytes;
      skipped = input.skip(skipLen, packetId - 1);
      assertEquals(curSkipped, skipped);
      assertEquals(curLocalSkipped, monitor._numSkippedLocalBytes);
      assertEquals(skipped, monitor._numSkippedWireBytes);
      assertEquals(numWirePackets, monitor._numWirePackets);
      assertEquals(2, monitor._numReattempts);
    } else {
      try {
        input.skip(skipLen, packetId++);
        fail("IOException should have been thrown");
      } catch(IOException e) {
        // success
      }
    }
    
    while((packet = input.readPacket(packetId++)) != null) {
      numWireBytes += packet.length;
      ++numWirePackets;
      if(!compress) {
        assertEquals(monitor._numWireBytes, monitor._numLocalBytes);
      } else {
        assertTrue(monitor._numWireBytes <= monitor._numLocalBytes);
      }
      assertEquals(numWireBytes, monitor._numWireBytes);
      assertEquals(numWirePackets, monitor._numWirePackets);
    }

    assertFalse(monitor._closed);

    input.close(true);

    assertTrue(monitor._closed);
    assertTrue(monitor._closedClean);

    assertEquals(FILE_SIZE, monitor._numLocalBytes + skipped);
    if(!compress) {
      assertEquals(FILE_SIZE, numWireBytes + skipped);
    } else {
      assertTrue(FILE_SIZE > numWireBytes + skipped);
    }
  }

  public void testOutputMonitor() throws Exception
  {
    doTestOutputMonitor(false);
    doTestOutputMonitor(true);
  }
  
  private void doTestOutputMonitor(boolean compress) throws Exception
  {
    AccumulateRemoteStreamMonitor<RemoteOutputStreamServer> monitor =
      new AccumulateRemoteStreamMonitor<RemoteOutputStreamServer>(false);

    InputStream localInput =
      new BufferedInputStream(new FileInputStream(TEST_FILE));
    RemoteInputStream input =
      (compress ?
       new GZIPRemoteInputStream(localInput) :
       new SimpleRemoteInputStream(localInput));
      
    File tempFile = File.createTempFile("dest", ".dat");
    tempFile.deleteOnExit();
    OutputStream localOutput =
      new BufferedOutputStream(new FileOutputStream(tempFile));
    RemoteOutputStream output =
      (compress ?
       new GZIPRemoteOutputStream(localOutput, monitor) :
       new SimpleRemoteOutputStream(localOutput, monitor));

    int inputPacketId = 0;
    byte[] packet = null;
    int packetId = 0;
    int numWireBytes = 0;
    int numWirePackets = 0;
    
    for(int i = 0; i < 5; ++i) {
      packet = input.readPacket(inputPacketId++);
      output.writePacket(packet, packetId++);
      numWireBytes += packet.length;
      ++numWirePackets;
      if(!compress) {
        assertEquals(monitor._numWireBytes, monitor._numLocalBytes);
      }
      assertEquals(numWireBytes, monitor._numWireBytes);
      assertEquals(numWirePackets, monitor._numWirePackets);
    }

    // rewrite packet
    int curLocalBytes = monitor._numLocalBytes;
    output.writePacket(packet, packetId - 1);
    assertEquals(curLocalBytes, monitor._numLocalBytes);
    assertEquals(numWireBytes, monitor._numWireBytes);
    assertEquals(numWirePackets, monitor._numWirePackets);

    while((packet = input.readPacket(inputPacketId++)) != null) {
      output.writePacket(packet, packetId++);
      numWireBytes += packet.length;
      ++numWirePackets;
      if(!compress) {
        assertEquals(monitor._numWireBytes, monitor._numLocalBytes);
      }
      assertEquals(numWireBytes, monitor._numWireBytes);
      assertEquals(numWirePackets, monitor._numWirePackets);
    }
    
    assertFalse(monitor._closed);

    output.close(true);

    assertTrue(monitor._closed);
    assertTrue(monitor._closedClean);

    assertEquals(FILE_SIZE, monitor._numLocalBytes);
    if(!compress) {
      assertEquals(FILE_SIZE, numWireBytes);
    } else {
      assertTrue(FILE_SIZE > numWireBytes);
    }
  }
  
  
}
