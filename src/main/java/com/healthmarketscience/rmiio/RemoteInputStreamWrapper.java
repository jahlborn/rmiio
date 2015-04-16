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

package com.healthmarketscience.rmiio;

import java.io.IOException;
import java.rmi.RemoteException;
import org.apache.commons.logging.Log;


/**
 * Wrapper for a RemoteInputStream stub which handles retry magic under the
 * hood.  The retry policy for a given method call will use the internal
 * policy for the default methods, but may be overridden on a per-call basis
 * using the extended methods.
 *
 * @author James Ahlborn
 */
public class RemoteInputStreamWrapper
  extends RemoteWrapper<RemoteInputStream>
  implements RemoteInputStream
{

  public RemoteInputStreamWrapper(RemoteInputStream stub,
                                  RemoteRetry retry,
                                  Log log) {
    super(stub, retry, log);
  }

  public boolean usingGZIPCompression()
    throws IOException
  {
    return usingGZIPCompression(_retry);
  }

  public boolean usingGZIPCompression(RemoteRetry retry)
    throws IOException
  {
    return retry.call(new RemoteRetry.Caller<Boolean>()
      {
        @Override
        public Boolean call() throws IOException {
          return _stub.usingGZIPCompression();
        }
      }, _log, RemoteException.class);
  }

  public int available()
    throws IOException
  {
    return available(_retry);
  }

  public int available(RemoteRetry retry)
    throws IOException
  {
    return retry.call(new RemoteRetry.Caller<Integer>()
      {
        @Override
        public Integer call() throws IOException {
          return _stub.available();
        }
      }, _log, IOException.class);
  }

  public void close(boolean readSuccess)
    throws IOException
  {
    close(readSuccess, _retry);
  }

  public void close(final boolean readSuccess, RemoteRetry retry)
    throws IOException
  {
    retry.call(new RemoteRetry.VoidCaller()
      {
        @Override
        public void voidCall() throws IOException {
          _stub.close(readSuccess);
        }
      }, _log, IOException.class);
  }

  public byte[] readPacket(int packetId)
    throws IOException
  {
    return readPacket(packetId, _retry);
  }

  public byte[] readPacket(final int packetId, RemoteRetry retry)
    throws IOException
  {
    return retry.call(new RemoteRetry.Caller<byte[]>()
      {
        @Override
        public byte[] call() throws IOException {
          return _stub.readPacket(packetId);
        }
      }, _log, IOException.class);
  }

  public long skip(long n, int skipId)
    throws IOException
  {
    return skip(n, skipId, _retry);
  }
  
  public long skip(final long n, final int skipId, RemoteRetry retry)
    throws IOException
  {
    return retry.call(new RemoteRetry.Caller<Long>()
      {
        @Override
        public Long call() throws IOException {
          return _stub.skip(n, skipId);
        }
      }, _log, IOException.class);
  }
  
}
