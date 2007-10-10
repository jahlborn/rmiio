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
 * @see <a href="package-summary.html#Usage_Notes">Usage Notes</a>
 *
 * @author James Ahlborn
 */
public class SerializableInputStream extends InputStream
  implements Serializable
{
  private static final long serialVersionUID = -8922181237767770749L;

  /** the handle to the actual remote interface */
  private final RemoteInputStream _remoteIn;
  /** optional client-side RemoteRetry policy */
  private transient RemoteRetry _retry;
  /** the actual client-side InputStream implementation, initialized on demand
      by a call to any one of the InputStream methods. */
  private transient InputStream _localIn;
  
  public SerializableInputStream(RemoteInputStream remoteIn) {
    _remoteIn = remoteIn;
  }

  /**
   * Initializes the actual client-side InputStream implementation if not
   * already initialized.  This call synchronizes on this object for the
   * initialization call only.  All other synchronization of actual stream
   * calls is handled by the implementation class created here.
   */
  private synchronized void initialize()
    throws IOException
  {
    if(_localIn == null) {
      _localIn = RemoteInputStreamClient.wrap(_remoteIn, _retry);
    }
  }

  /**
   * May be called on the client-side in order to set the RemoteRetry policy
   * used by the underlying implementation.  <b>Must</b> be called prior to
   * any stream method call, as once the underlying stream is initialized, the
   * RemoteRetry policy cannot be changed.
   * @param retry the RemoteRetry policy to be used for all remote method
   *              calls made by this class.  may be <code>null</code>, in
   *              which case the {@link RemoteInputStreamClient#DEFAULT_RETRY}
   *              policy will be used.
   */
  public synchronized void setRetry(RemoteRetry retry) {
    _retry = retry;
  }

  @Override
  public int available()
    throws IOException
  {
    initialize();
    return _localIn.available();
  }

  @Override
  public int read()
    throws IOException
  {
    initialize();
    return _localIn.read();
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
    initialize();
    return _localIn.read(buf, pos, len);
  }

  @Override
  public long skip(long len)
    throws IOException
  {
    initialize();
    return _localIn.skip(len);
  }

  @Override
  public void close()
    throws IOException
  {
    initialize();
    _localIn.close();
  }
  
}
