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

package com.healthmarketscience.rmiio.exporter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import com.healthmarketscience.rmiio.RemoteInputStream;
import com.healthmarketscience.rmiio.RemoteInputStreamServer;
import com.healthmarketscience.rmiio.RemoteOutputStream;
import com.healthmarketscience.rmiio.RemoteOutputStreamServer;
import com.healthmarketscience.rmiio.SimpleRemoteInputStream;
import com.healthmarketscience.rmiio.SimpleRemoteOutputStream;
import junit.framework.TestCase;

/**
 * @author James Ahlborn
 */
public class RemoteStreamProxyTest extends TestCase
{

  public void testInputStreamProxy() throws Exception
  {
    byte[] testBytes = "this is a test".getBytes();
    RemoteInputStream directStream = new SimpleRemoteInputStream(
        new ByteArrayInputStream(testBytes));
    RemoteInputStream proxyStream = new TestInputProxy(
        new SimpleRemoteInputStream(
            new ByteArrayInputStream(testBytes)));

    assertEquals(directStream.available(), proxyStream.available());
    assertEquals(directStream.usingGZIPCompression(),
                 proxyStream.usingGZIPCompression());
    assertEquals(directStream.skip(4, 0), proxyStream.skip(4, 0));

    byte[] directPacket = directStream.readPacket(1);
    byte[] proxyPacket = proxyStream.readPacket(1);
    byte[] expectedPacket = new byte[testBytes.length - 4];
    System.arraycopy(testBytes, 4, expectedPacket, 0, expectedPacket.length);
    checkPacket(expectedPacket, directPacket, proxyPacket);

    try {
      proxyStream.readPacket(0);
      fail("IllegalArgumentException should have been thrown");
    } catch(IllegalArgumentException e) {
      // success
    }
    
    directStream.close(true);
    proxyStream.close(true);
  }

  public void testOutputStreamProxy() throws Exception
  {
    byte[] testBytes = "this is a test".getBytes();

    TestOutputStream directOStream = new TestOutputStream();
    TestOutputStream proxyOStream = new TestOutputStream();
    
    RemoteOutputStream directStream = new SimpleRemoteOutputStream(
        directOStream);
    RemoteOutputStream proxyStream = new TestOutputProxy(
        new SimpleRemoteOutputStream(proxyOStream));

    assertEquals(directStream.usingGZIPCompression(),
                 proxyStream.usingGZIPCompression());

    directStream.writePacket(testBytes, 1);
    proxyStream.writePacket(testBytes, 1);

    assertFalse(directOStream._flushed);
    assertFalse(proxyOStream._flushed);
    
    directStream.flush();
    proxyStream.flush();

    assertTrue(directOStream._flushed);
    assertTrue(proxyOStream._flushed);
    
    byte[] directPacket = directOStream.toByteArray();
    byte[] proxyPacket = proxyOStream.toByteArray();
    byte[] expectedPacket = testBytes;
    checkPacket(expectedPacket, directPacket, proxyPacket);

    try {
      proxyStream.writePacket(testBytes, 0);
      fail("IllegalArgumentException should have been thrown");
    } catch(IllegalArgumentException e) {
      // success
    }
    
    directStream.close(true);
    proxyStream.close(true);
  }

  public void testInvalidMethod() throws Exception
  {
    byte[] testBytes = "this is a test".getBytes();
    RemoteInputStreamServer inServer = new SimpleRemoteInputStream(
        new ByteArrayInputStream(testBytes));
    try {
      RemoteStreamServerInvokerHelper.invoke(inServer, 42,
                                             new Object[0]);
      fail("IllegalArgumentException should have been thrown");
    } catch(IllegalArgumentException e) {
      // success
    }
    
    RemoteOutputStreamServer outServer = new SimpleRemoteOutputStream(
        new ByteArrayOutputStream());
    try {
      RemoteStreamServerInvokerHelper.invoke(outServer, 42,
                                             new Object[0]);
      fail("IllegalArgumentException should have been thrown");
    } catch(IllegalArgumentException e) {
      // success
    }
  }
  
  private void checkPacket(byte[] expected, byte[] direct, byte[] proxy)
  {
    assertTrue(Arrays.equals(direct, proxy));
    assertTrue(Arrays.equals(expected, proxy));
  }
  
  private static class TestInputProxy extends RemoteInputStreamClientProxy
  {
    private final RemoteInputStreamServer _server;
    
    private TestInputProxy(RemoteInputStreamServer server)
    {
      _server = server;
    }
    
    @Override
    protected Object invoke(int methodCode, Object... parameters)
      throws IOException
    {
      return RemoteStreamServerInvokerHelper.invoke(
          _server, methodCode, parameters);
    }

  }
  
  private static class TestOutputProxy extends RemoteOutputStreamClientProxy
  {
    private final RemoteOutputStreamServer _server;
    
    private TestOutputProxy(RemoteOutputStreamServer server)
    {
      _server = server;
    }
    
    @Override
    protected Object invoke(int methodCode, Object... parameters)
      throws IOException
    {
      return RemoteStreamServerInvokerHelper.invoke(
          _server, methodCode, parameters);
    }

  }

  private static class TestOutputStream extends ByteArrayOutputStream
  {
    boolean _flushed;
    
    @Override
    public void flush() throws IOException
    {
      _flushed = true;
      super.flush();
    }
  }
  
}
