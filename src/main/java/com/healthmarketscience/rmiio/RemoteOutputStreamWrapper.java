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
 * Wrapper for a RemoteOutputStream stub which handles retry magic under the
 * hood.  The retry policy for a given method call will use the internal
 * policy for the default methods, but may be overridden on a per-call basis
 * using the extended methods.
 *
 * @author James Ahlborn
 */
public class RemoteOutputStreamWrapper
  extends RemoteWrapper<RemoteOutputStream>
  implements RemoteOutputStream
{

  public RemoteOutputStreamWrapper(RemoteOutputStream stub,
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

  public void close(boolean writeSuccess)
    throws IOException
  {
    close(writeSuccess, _retry);
  }

  public void close(final boolean writeSuccess, RemoteRetry retry)
    throws IOException
  {
    retry.call(new RemoteRetry.VoidCaller()
      {
        @Override
        public void voidCall() throws IOException {
          _stub.close(writeSuccess);
        }
      }, _log, IOException.class);
  }

  public void flush()
    throws IOException
  {
    flush(_retry);
  }

  public void flush(RemoteRetry retry)
    throws IOException
  {
    retry.call(new RemoteRetry.VoidCaller()
      {
        @Override
        public void voidCall() throws IOException {
          _stub.flush();
        }
      }, _log, IOException.class);
  }

  public void writePacket(byte[] packet, int packetId)
    throws IOException
  {
    writePacket(packet, packetId, _retry);
  }

  public void writePacket(final byte[] packet, final int packetId,
                          RemoteRetry retry)
    throws IOException
  {
    retry.call(new RemoteRetry.VoidCaller()
      {
        @Override
        public void voidCall() throws IOException {
          _stub.writePacket(packet, packetId);
        }
      }, _log, IOException.class);
  }

}
