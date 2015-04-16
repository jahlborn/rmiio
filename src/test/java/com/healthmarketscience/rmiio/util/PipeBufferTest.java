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

package com.healthmarketscience.rmiio.util;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Arrays;

import junit.framework.TestCase;

/**
 * @author James Ahlborn
 */
public class PipeBufferTest extends TestCase {

  public PipeBufferTest(String name) {
    super(name);
  }

  public void testEmpty()
  {
    PipeBuffer pb = new PipeBuffer();

    assertTrue(!pb.hasRemaining());
    
    byte[] tmp = new byte[1024];
    pb.read(tmp, 0, 0);

    assertTrue(!pb.hasRemaining());

    pb.write(tmp, 0, 0);

    assertTrue(!pb.hasRemaining());

    pb.read(tmp, 0, 0);

    assertTrue(!pb.hasRemaining());

    pb.writePacket(tmp, 0, 0);
    
    assertTrue(!pb.hasRemaining());
  }

  public void testReadWrite() throws Exception
  {
    PipeBuffer pb = new PipeBuffer();

    ByteArrayOutputStream testInBytes = new ByteArrayOutputStream(10000);
    ByteArrayOutputStream testOutBytes = new ByteArrayOutputStream(10000);
    
    writeBytes(13, pb, testInBytes);
    writeBytes(45, pb, testInBytes);
    writeBytes(1024, pb, testInBytes);
    readBytes(100, pb, testOutBytes);
    writeBytes(7053, pb, testInBytes);
    writeBytes(42, pb, testInBytes);
    readBytes(7053, pb, testOutBytes);
    writeBytes(7053, pb, testInBytes);
    readBytes((int)pb.remaining(), pb, testOutBytes);
    writeBytes(7053, pb, testInBytes);
    readBytes(400, pb, testOutBytes);
    writeBytes(13, pb, testInBytes);
    writeBytes(13, pb, testInBytes);
    readBytes((int)pb.remaining(), pb, testOutBytes);

    byte[] writeBytes = testInBytes.toByteArray();
    byte[] readBytes = testOutBytes.toByteArray();
    assertTrue(Arrays.equals(writeBytes, readBytes));    
  }

  public void testReadWritePackets() throws Exception
  {
    PipeBuffer pb = new PipeBuffer();

    ByteArrayOutputStream testInBytes = new ByteArrayOutputStream(10000);
    ByteArrayOutputStream testOutBytes = new ByteArrayOutputStream(10000);
    
    writePacket(13, pb, testInBytes);
    assertEquals(0, pb.packetsAvailable());
    writePacket(44, pb, testInBytes);
    assertEquals(1, pb.packetsAvailable());
    writePacket(1024, pb, testInBytes);
    assertEquals(3, pb.packetsAvailable());
    readPacket(pb, testOutBytes);
    writePacket(7053, pb, testInBytes);
    writePacket(42, pb, testInBytes);
    readPacket(pb, testOutBytes);
    writePacket(7053, pb, testInBytes);
    while(pb.hasRemaining()) {
      readPacket(pb, testOutBytes);
    }
    writePacket(7053, pb, testInBytes);
    readPacket(pb, testOutBytes);
    writePacket(13, pb, testInBytes);
    writePacket(13, pb, testInBytes);
    while(pb.hasRemaining()) {
      readPacket(pb, testOutBytes);
    }

    byte[] writeBytes = testInBytes.toByteArray();
    byte[] readBytes = testOutBytes.toByteArray();
    assertTrue(Arrays.equals(writeBytes, readBytes));        
  }

  public void testReadWriteMixed() throws Exception
  {
    PipeBuffer pb = new PipeBuffer();

    ByteArrayOutputStream testInBytes = new ByteArrayOutputStream(10000);
    ByteArrayOutputStream testOutBytes = new ByteArrayOutputStream(10000);
    
    writePacket(13, pb, testInBytes);
    assertEquals(0, pb.packetsAvailable());
    writeBytes(44, pb, testInBytes);
    assertEquals(1, pb.packetsAvailable());
    writePacket(1024, pb, testInBytes);
    assertEquals(3, pb.packetsAvailable());
    readBytes(100, pb, testOutBytes);
    readPacket(pb, testOutBytes);
    writeBytes(7053, pb, testInBytes);
    writePacket(42, pb, testInBytes);
    readBytes(2013, pb, testOutBytes);
    readBytes(2015, pb, testOutBytes);
    writePacket(7053, pb, testInBytes);
    while(pb.hasRemaining()) {
      readBytes((int)(pb.remaining() / 2), pb, testOutBytes);
      readPacket(pb, testOutBytes);
    }
    writeBytes(7053, pb, testInBytes);
    readPacket(pb, testOutBytes);
    writePacket(13, pb, testInBytes);
    writePacket(13, pb, testInBytes);
    while(pb.hasRemaining()) {
      readBytes((int)(pb.remaining() / 2), pb, testOutBytes);
      readPacket(pb, testOutBytes);
    }

    byte[] writeBytes = testInBytes.toByteArray();
    byte[] readBytes = testOutBytes.toByteArray();
    assertTrue(Arrays.equals(writeBytes, readBytes));        
  }

  public void testSkip() throws Exception
  {
    PipeBuffer pb = new PipeBuffer();
    pb.skip(0);

    byte[] bIn = new byte[]{13, 47, 118, 34, 21};
    
    pb.write(bIn, 0, bIn.length);
    pb.writePacket(bIn, 2, 3);

    byte[] bOut = new byte[4];

    pb.read(bOut, 0, 3);
    pb.skip(4);
    pb.read(bOut, 3, 1);

    assertTrue(!pb.hasRemaining());
    assertTrue(Arrays.equals(new byte[]{13, 47, 118, 21}, bOut));

  }

  
  private void writeBytes(int length, PipeBuffer pb, OutputStream testStream)
    throws Exception
  {
    byte[] bytes = new byte[length];
    for(int i = 0; i < length; ++i) {
      bytes[i] = (byte)i;
    }

    pb.write(bytes, 0, length);

    // write bytes to test stream
    testStream.write(bytes, 0, length);
  }

  private void readBytes(int length, PipeBuffer pb, OutputStream testStream)
    throws Exception
  {
    byte[] bytes = new byte[length];

    pb.read(bytes, 0, length);

    // write bytes to test stream
    testStream.write(bytes, 0, length);
  }

  private void writePacket(int length, PipeBuffer pb, OutputStream testStream)
    throws Exception
  {
    int pos = (length % 3);
    byte[] bytes = new byte[pos + (int)(length * 1.13)];
    for(int i = pos; i < (pos + length); ++i) {
      bytes[i] = (byte)i;
    }

    pb.writePacket(bytes, pos, length);

    // write bytes to test stream
    testStream.write(bytes, pos, length);
  }

  private void readPacket(PipeBuffer pb, OutputStream testStream)
    throws Exception
  {
    byte[] bytes = pb.readPacket();

    // write bytes to test stream
    testStream.write(bytes, 0, bytes.length);
  }
  
}
