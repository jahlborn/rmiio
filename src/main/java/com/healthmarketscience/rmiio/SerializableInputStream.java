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
import java.io.InputStream;
import java.io.Serializable;

/**
 * An additional layer around a RemoteInputStream which makes it Serializable
 * and an InputStream.  In general, this extra layer is not necessary and
 * <em>I do not recommend using this class</em>.  However, in the odd case
 * where the callee really wants to get something which is already an
 * InputStream, this class can be useful.  This is basically just a wrapper
 * around a call to {@link RemoteInputStreamClient#wrap}.
 *
 * @see <a href="{@docRoot}/overview-summary.html#Usage_Notes">Usage Notes</a>
 *
 * @author James Ahlborn
 */
public class SerializableInputStream extends InputStream
  implements Serializable, RemoteClient
{
  private static final long serialVersionUID = -8922181237767770749L;

  /** the handle to the actual remote interface */
  private final RemoteInputStream _remoteIn;
  /** optional client-side RemoteRetry policy */
  private transient RemoteRetry _retry;
  /** the actual client-side InputStream implementation, initialized on demand
      by a call to any one of the InputStream methods. */
  private transient InputStream _localIn;
  
  public SerializableInputStream(InputStream localIn)
    throws IOException
  {
    // note, we do not need to export here, as it will be handled
    // automagically when the _remoteIn field is serialized.  this makes it
    // very easy to consume this input stream locally or remotely.
    this(new GZIPRemoteInputStream(localIn));
  }

  public SerializableInputStream(RemoteInputStream remoteIn) {
    if(remoteIn == null) {
      throw new IllegalArgumentException("InputStream cannot be null");
    }
    _remoteIn = remoteIn;
  }

  /**
   * @return the the actual client-side InputStream implementation, creating
   *         if necessary.  This call synchronizes on this object for the
   *         initialization call only.  All other synchronization of actual
   *         stream calls is handled by the implementation class created here.
   */
  private synchronized InputStream getLocalIn()
    throws IOException
  {
    if(_localIn == null) {
      _localIn = RemoteInputStreamClient.wrap(_remoteIn, _retry);
    }
    return _localIn;
  }

  public synchronized void setRemoteRetry(RemoteRetry retry) {
    _retry = retry;
  }

  @Override
  public int available()
    throws IOException
  {
    return getLocalIn().available();
  }

  @Override
  public int read()
    throws IOException
  {
    return getLocalIn().read();
  }

  @Override
  public int read(byte[] b)
    throws IOException
  {
    return read(b, 0, b.length);
  }

  @Override
  public int read(byte[] buf, int pos, int len)
    throws IOException
  {
    return getLocalIn().read(buf, pos, len);
  }

  @Override
  public long skip(long len)
    throws IOException
  {
    return getLocalIn().skip(len);
  }

  @Override
  public void close()
    throws IOException
  {
    getLocalIn().close();
  }
  
}
