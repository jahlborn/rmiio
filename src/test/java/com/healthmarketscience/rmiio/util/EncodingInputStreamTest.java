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
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import com.healthmarketscience.rmiio.BaseRemoteStreamTest;
import com.healthmarketscience.rmiio.PacketOutputStream;
import junit.framework.TestCase;

/**
 * @author James Ahlborn
 */
public class EncodingInputStreamTest extends TestCase
{

  public EncodingInputStreamTest(String name) {
    super(name);
  }

  public void testReadPackets() throws Exception
  {
    // packets should be passed through
    TestEncodingInputStream istream = new TestEncodingInputStream();
    byte[] writePacket = new byte[1024];
    istream._toWrite = writePacket;
    istream._writePacket = true;
    byte[] readPacket = istream.readPacket();
    assertTrue(readPacket == writePacket);
  }

  public void testRead() throws Exception
  {
    TestEncodingInputStream istream = new TestEncodingInputStream();

    ByteArrayOutputStream testInBytes = new ByteArrayOutputStream(10000);
    ByteArrayOutputStream testOutBytes = new ByteArrayOutputStream(10000);
    
    writeBytes(13, istream, testInBytes);
    readBytes(13, istream, testOutBytes, false);
    writeBytes(42, istream, testInBytes);
    readBytes(42, istream, testOutBytes, false);
    writeBytes(1024, istream, testInBytes);
    readBytes(1024, istream, testOutBytes, false);
    writeBytes(7053, istream, testInBytes);
    readBytes(7053, istream, testOutBytes, false);
    writeBytes(1024, istream, testInBytes);
    readBytes(1024, istream, testOutBytes, false);
    writeBytes(42, istream, testInBytes);
    readBytes(42, istream, testOutBytes, false);
    writeBytes(7053, istream, testInBytes);
    readBytes(7053, istream, testOutBytes, false);
    writeBytes(7053, istream, testInBytes);
    readBytes(7053, istream, testOutBytes, false);
    writeBytes(13, istream, testInBytes);
    readBytes(13, istream, testOutBytes, false);
    writeBytes(13, istream, testInBytes);
    readBytes(13, istream, testOutBytes, false);
    readBytes(109, istream, testOutBytes, true);

    byte[] writeBytes = testInBytes.toByteArray();
    byte[] readBytes = testOutBytes.toByteArray();
    assertTrue(Arrays.equals(writeBytes, readBytes));
  }

  public void testSkip() throws Exception
  {
    TestEncodingInputStream istream = new TestEncodingInputStream();

    ByteArrayOutputStream testInBytes = new ByteArrayOutputStream(10000);
    ByteArrayOutputStream testOutBytes = new ByteArrayOutputStream(10000);
    
    writeBytes(100, istream, testInBytes);
    readBytes(49, istream, testOutBytes, false);
    writeBytes(100, istream, testInBytes);
    assertEquals(100, istream.skip(100));
    readBytes(10, istream, testOutBytes, true);

    byte[] writeBytes = testInBytes.toByteArray();
    byte[] readBytes = testOutBytes.toByteArray();
    assertEquals(writeBytes.length, readBytes.length + 100);

    byte[] writeBytes2 = new byte[readBytes.length];
    System.arraycopy(writeBytes, 0, writeBytes2, 0, 49);
    System.arraycopy(writeBytes, 149, writeBytes2, 49, 51);
    assertTrue(Arrays.equals(writeBytes2, readBytes));
    
  }
  
  private void writeBytes(int length, TestEncodingInputStream istream,
                          OutputStream testStream)
    throws Exception
  {
    byte[] bytes = new byte[length];
    for(int i = 0; i < length; ++i) {
      bytes[i] = (byte)i;
    }

    istream._toWrite = bytes;
    istream._writePacket = false;

    // write bytes to test stream
    testStream.write(bytes);

    ++istream._iteration;
  }

  private void readBytes(int length, TestEncodingInputStream istream,
                         OutputStream testStream, boolean finish)
    throws Exception
  {
    length = (int)(length * 0.75);
    if(length == 0) length = 1;

    byte[] bytes = new byte[length];
    int bytesRead; 
    while((bytesRead = BaseRemoteStreamTest.cycleRead(
               istream, bytes, istream._iteration)) >= 0) {
      // write bytes to test stream
      testStream.write(bytes, 0, bytesRead);
      
      if(!finish && (istream._toWrite == null)) {
        // stop when the current bytes have been written, unless we are
        // finishing
        break;
      }
    }

    ++istream._iteration;
  }

  private class TestEncodingInputStream extends EncodingInputStream
  {
    private PacketOutputStream _ostream;
    public byte[] _toWrite;
    public boolean _writePacket;
    public int _iteration = 0;

    private TestEncodingInputStream() {
      _ostream = createOutputStream();
    }
    
    @Override
    public void encode(int suggestedLength)
      throws IOException
    {
      if(_toWrite == null) {
        _ostream.close();
        return;
      }
      
      if(_writePacket) {
        _ostream.writePacket(_toWrite);
      } else {
        BaseRemoteStreamTest.cycleWrite(_ostream, _toWrite,
                                        _toWrite.length, _iteration);
      }
      _toWrite = null;
    }
  }
    
}
