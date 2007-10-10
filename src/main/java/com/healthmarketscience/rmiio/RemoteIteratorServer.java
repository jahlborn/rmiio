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

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

import com.healthmarketscience.rmiio.exporter.RemoteStreamExporter;


/**
 * Base class for implementing the server side of a RemoteIterator.  This
 * object manages RemoteInputStreamServer which connects the client and the
 * server.  Implementations of this class must provide an InputStream which is
 * the source of the data going over the wire.  Note, users of this class
 * should ensure that the close() method is called one way or another, or
 * shutdown of the process may be delayed.
 *
 * @author James Ahlborn
 */
public class RemoteIteratorServer<DataType> implements Closeable
{

  /** InputStream which is used by the RemoteInputStreamServer to read the
      objects. */
  protected final InputStream _localIStream;
  /** handle to the remote pipe linking this class to the client */
  private final RemoteInputStreamServer _remoteIStream;

  public RemoteIteratorServer(InputStream localIStream)
    throws IOException
  {
    this(localIStream, true, RemoteInputStreamServer.DUMMY_MONITOR);
  }
  
  public RemoteIteratorServer(InputStream localIStream, boolean useCompression)
    throws IOException
  {
    this(localIStream, useCompression, RemoteInputStreamServer.DUMMY_MONITOR);
  }
  
  public RemoteIteratorServer(
      InputStream localIStream,
      boolean useCompression,
      RemoteStreamMonitor<RemoteInputStreamServer> monitor)
    throws IOException
  {
    this(localIStream, useCompression, monitor,
         RemoteInputStreamServer.DEFAULT_CHUNK_SIZE);
  }

  public RemoteIteratorServer(
      InputStream localIStream,
      boolean useCompression,
      RemoteStreamMonitor<RemoteInputStreamServer> monitor,
      int chunkSize)
    throws IOException
  {
    _localIStream = localIStream;
    if(useCompression) {
      _remoteIStream = new GZIPRemoteInputStream(_localIStream, monitor,
                                                 chunkSize);
    } else {
      _remoteIStream = new SimpleRemoteInputStream(_localIStream, monitor,
                                                   chunkSize);
    }
  }

  /**
   * Package-protected: gets handle to the RemoteInputStream which the
   * RemoteInputStreamClient should use.  Does the export of the object, so
   * should only be called <b>once</b> by the client.
   * @param exporter the exporter which will handle exporting the underlying
   *                 RemoteInputStream on the appropriate RPC framework
   */
  RemoteInputStream getRemoteInputStream(RemoteStreamExporter exporter)
    throws IOException
  {
    return((exporter == null) ? _remoteIStream.export() :
           exporter.export(_remoteIStream));
  }

  /**
   * @return <code>true</code> iff this iterator server has been closed (one
   *         way or another), <code>false</code> otherwise.
   */
  public boolean isClosed() {
    return _remoteIStream.isClosed();
  }

  /**
   * Forces this iterator server to close (if not already closed), will
   * <b>break</b> any outstanding client interactions.  Should be called one
   * way or another on the server object (may be left to the underlying stream
   * <code>unreferenced</code> method if the server object must live beyond
   * the creation method call).
   */
  public void close() {
    _remoteIStream.close();
  }
  
  /**
   * Aborts the current transfer without closing this RemoteIteratorServer.
   * This method is thread safe.  This will usually shutdown a currently
   * working transfer faster than just closing the RemoteIteratorServer
   * directly (because this will cause the client to get an IOException
   * instead of a RemoteException, which may cause retries, etc.).  This
   * RemoteIteratorServer should still be closed as normal.
   */
  public void abort()
    throws IOException
  {
    _remoteIStream.abort();
  }

  /**
   * Utility method to choose a RemoteStreamMonitor.  If none is explicitly
   * provided and the localIterator implements RemoteStreamMonitor, use it
   * instead.
   * @param monitor the monitor provided in the constrctor
   * @param localSource the actual source of information for the iterator
   * @return an appropriate RemoteStreamMonitor for use with the underlying
   *         stream
   */
  @SuppressWarnings("unchecked")
  public static RemoteStreamMonitor<RemoteInputStreamServer> chooseMonitor(
      RemoteStreamMonitor<RemoteInputStreamServer> monitor,
      Object localSource)
  {
    // a common idiom is for the localSource to implement RemoteStreamMonitor
    // if it needs to manage local resources.  check for this if no monitor
    // (or the DUMMY_MONITOR) was explicitly provided.
    if(((monitor == null) ||
        (monitor == RemoteInputStreamServer.DUMMY_MONITOR)) &&
       (localSource instanceof RemoteStreamMonitor)) {
      monitor = (RemoteStreamMonitor<RemoteInputStreamServer>)localSource;
    }
    return monitor;
  }
    
}
