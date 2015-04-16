/*
Copyright (c) 2012 James Ahlborn

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

package com.healthmarketscience.rmiio.socket;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.Serializable;

import com.healthmarketscience.rmiio.RemoteClient;
import com.healthmarketscience.rmiio.RemoteInputStreamServer;
import com.healthmarketscience.rmiio.RemoteOutputStream;
import com.healthmarketscience.rmiio.RemoteOutputStreamClient;
import com.healthmarketscience.rmiio.RemoteRetry;
import com.healthmarketscience.rmiio.RmiioUtil;
import com.healthmarketscience.rmiio.SimpleRemoteOutputStream;

/**
 * Utility class which can be used to simulate a socket-like connection over
 * RMI.  A single RMISocket instance enables only a single direction of
 * communication (from the remote system to this system), in which case the
 * instantiator of the RMISocket is an RMI server (essentially serving a
 * {@link RemoteOutputStream}).  In order for bi-directional communication to
 * take place, each system must have an instance of RMISocket and the relevant
 * Sources should be exchanged, in which case <i>both</i> systems will be
 * acting as RMI servers.
 * <p/>
 * In general, simulating a socket connection over RMI is probably not the
 * best idea, and should not be pursued for a <i>new</i> project.  However,
 * when revamping an existing project, it may be desirable to layer an
 * existing socket-based protocol over a separately established RMI
 * connection.  In such a situation, this utility could be very useful.
 *
 * @author James Ahlborn
 */
public class RMISocket implements Closeable
{
  /** local InputStream for receiving data from local Source which has been
      passed to remote system */
  private final PipedInputStream _in;
  /** the local source for this socket which must be passed to the remote
      system */
  private final Source _source;
  /** optional source for the remote system allowing bi-directional
      communication */
  private Source _remoteSource;

  public RMISocket() throws IOException {
    this(null);
  }

  public RMISocket(Source remoteSource) throws IOException {
    _in = new PipedInputStream(RemoteInputStreamServer.DEFAULT_CHUNK_SIZE);
    PipedOutputStream out = new PipedOutputStream(_in);
    _source = new Source(new SimpleRemoteOutputStream(out));
    _remoteSource = remoteSource;
  }

  /**
   * Returns the InputStream which can be used to read data sent through the
   * socket via the local Source which has been passed to the remote system.
   */
  public InputStream getInputStream() throws IOException {
    return _in;
  }

  /**
   * Returns the OutputStream of the remote Source, if one has been associated
   * with this socket (thus allowing bi-directional communication).
   *
   * @return the OutputStream for the remote Source
   */
  public OutputStream getOutputStream() throws IOException {
    if(_remoteSource == null) {
      throw new IllegalStateException("no remote source has been associated with this socket");
    }
    return _remoteSource.getOutputStream();
  }

  /**
   * Returns the local Source for this socket.  This must be passed to the
   * remote system so that it can send data through this socket.
   */
  public Source getSource() {
    return _source;
  }

  /**
   * Closes the local InputStream and the remote Source if one has been
   * associated with this socket.
   */
  public void close() throws IOException {
    try {
      if(_remoteSource != null) {
        _remoteSource.close();
      }
    } finally {
      RmiioUtil.closeQuietly(_in);
    }   
  }

  /**
   * Returns the remote Source associated with this socket, if any.  This must
   * be passed to the remote system in order for this socket to function.
   */
  public Source getRemoteSource() {
    return _remoteSource;
  }

  /**
   * Associates a remote Source with this socket, allowing it to be
   * bi-directional (at which point the {@link #getOutputStream} method may be
   * called).
   */
  public void setRemoteSource(Source newRemoteSource) {
    _remoteSource = newRemoteSource;
  }

  /**
   * Serializable stub which must be passed to the remote system in order to
   * enable it to send data through this socket.
   */
  public static class Source implements Serializable, RemoteClient, Closeable
  {
    private static final long serialVersionUID = 20120625L;    

    /** the handle to the actual remote interface */
    private final RemoteOutputStream _remoteOut;
    /** optional client-side RemoteRetry policy */
    private transient RemoteRetry _retry;
    /** the actual client-side OutputStream implementation, initialized on
        demand by a call to getOutputStream */
    private transient OutputStream _localOut;
    /** optional client-side target value for the byte size of the packets of
        data sent over the wire */
    private transient Integer _chunkSize;

    private Source(RemoteOutputStream out) {
      _remoteOut = out;
    }

    public OutputStream getOutputStream() throws IOException {
      if(_localOut == null) {
        _localOut = RemoteOutputStreamClient.wrap(_remoteOut, _retry,
                                                  _chunkSize);
      }
      return _localOut;
    }

    public void setRemoteRetry(RemoteRetry retry) {
      _retry = retry;
    }
    
    /**
     * May be called on the client-side in order to set the target chunk size
     * used by the underlying implementation.  <b>Must</b> be called prior to
     * the first call to {@link #getOutputStream}, as once the underlying
     * stream is initialized, the chunk size cannot be changed.
     * @param chunkSize target value for the byte size of the packets of data
     *                  sent over the wire.  note that this is a suggestion,
     *                  actual packet sizes may vary.  if <code>null</code>,
     *                  {@link RemoteOutputStreamClient#DEFAULT_CHUNK_SIZE}
     *                  will be used.
     */
    public void setChunkSize(Integer chunkSize) {
      _chunkSize = chunkSize;
    }

    public void close() throws IOException {
      getOutputStream().close();
    }
  }
}
