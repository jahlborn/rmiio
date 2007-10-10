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
import java.io.OutputStream;
import java.io.Serializable;

/**
 * An additional layer around a RemoteOutputStream which makes it Serializable
 * and an OutputStream.  In general, this extra layer is not necessary and
 * <em>I do not recommend using this class</em>.  However, in the odd case
 * where the callee really wants to get something which is already an
 * OutputStream, this class can be useful.  This is basically just a wrapper
 * around a call to {@link RemoteOutputStreamClient#wrap}.
 *
 * @see <a href="package-summary.html#Usage_Notes">Usage Notes</a>
 *
 * @author James Ahlborn
 */
public class SerializableOutputStream extends OutputStream
  implements Serializable
{
  private static final long serialVersionUID = 2752774698731338838L;
  
  /** the handle to the actual remote interface */
  private final RemoteOutputStream _remoteOut;
  /** optional client-side RemoteRetry policy */
  private transient RemoteRetry _retry;
  /** the actual client-side OutputStream implementation, initialized on demand
      by a call to any one of the OutputStream methods. */
  private transient OutputStream _localOut;
  /** optional client-side target value for the byte size of the packets of
      data sent over the wire */
  private transient Integer _chunkSize;
  
  public SerializableOutputStream(RemoteOutputStream remoteOut) {
    _remoteOut = remoteOut;
  }

  /**
   * Initializes the actual client-side OutputStream implementation if not
   * already initialized.  This call synchronizes on this object for the
   * initialization call only.  All other synchronization of actual stream
   * calls is handled by the implementation class created here.
   */
  private synchronized void initialize()
    throws IOException
  {
    if(_localOut == null) {
      _localOut = RemoteOutputStreamClient.wrap(_remoteOut, _retry,
                                                _chunkSize);
    }
  }

  /**
   * May be called on the client-side in order to set the RemoteRetry policy
   * used by the underlying implementation.  <b>Must</b> be called prior to
   * any stream method call, as once the underlying stream is initialized, the
   * RemoteRetry policy cannot be changed.
   * @param retry the RemoteRetry policy to be used for all remote method
   *              calls made by this class.  if <code>null</code>, 
   *              {@link RemoteOutputStreamClient#DEFAULT_RETRY}
   *              policy will be used.
   */
  public synchronized void setRetry(RemoteRetry retry) {
    _retry = retry;
  }

  /**
   * May be called on the client-side in order to set the target chunk size
   * used by the underlying implementation.  <b>Must</b> be called prior to
   * any stream method call, as once the underlying stream is initialized, the
   * chunk size cannot be changed.
   * @param chunkSize target value for the byte size of the packets of data
   *                  sent over the wire.  note that this is a suggestion,
   *                  actual packet sizes may vary.  if <code>null</code>,
   *                  {@link RemoteOutputStreamClient#DEFAULT_CHUNK_SIZE}
   *                  will be used.
   */
  public synchronized void setChunkSize(Integer chunkSize) {
    _chunkSize = chunkSize;
  }

  @Override
  public void flush()
    throws IOException
  {
    initialize();
    _localOut.flush();
  }
    
  @Override
  public void write(int b)
    throws IOException
  {
    initialize();
    _localOut.write(b);
  }

  @Override
  public void write(byte[] b)
    throws IOException
  {
    write(b, 0, b.length);
  }

  @Override
  public void write(byte[] b, int off, int len)
    throws IOException
  {
    initialize();
    _localOut.write(b, off, len);
  }
  
  @Override
  public void close()
    throws IOException
  {
    initialize();
    _localOut.close();
  }  
}
