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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.NoSuchElementException;

import com.healthmarketscience.rmiio.exporter.RemoteStreamExporter;


/**
 * Base class for implementing the client side of a RemoteIterator.  This
 * object is serialized and sent to the consumer of the data.  The consumer
 * then pulls objects through the internal RemoteInputStream as needed.
 * Implementations of this class must implement the
 * {@link #initialize(InputStream)} and {@link #readNextObject} methods. The
 * initialize() method is called once before the first call to
 * readNextObject() and gives the subclass access to the remote input stream.
 * The readNextObject() method should read an object from the underlying input
 * stream and return it.  When all data is read, an EOFException should be
 * thrown indicating that the iterator is finished.  A subclass may also
 * override closeIterator() to do any final cleanup (called when the
 * EOFException is thrown).
 *
 * @author James Ahlborn
 */
public abstract class RemoteIteratorClient<DataType>
  implements RemoteIterator<DataType>, Serializable
{
  private static final long serialVersionUID = -7068967719628663585L;
  
  /** handle to the remote pipe linking this class to the server */
  private final RemoteInputStream _remoteIStream;
  /** handle to the local wrapper around the remote input stream */
  private transient InputStream _localIStream;
  /** iff <code>false</code>, initialization has not been done yet. */
  private transient boolean _initialized;
  /** iff <code>true</code>, there are more elements left in the iteration. */
  private transient boolean _hasNext;
  /** the next object to return.  (<code>null</code> is a *valid* value, it
      does not indicate the end of the iteration. */
  private transient DataType _nextObj;
  /** the client-side RemoteRetry policy to use for the remote communication
      layer. */
  private transient RemoteRetry _remoteRetry;

  protected RemoteIteratorClient(RemoteIteratorServer<DataType> server)
    throws IOException
  {
    this(server, null);
  }
  
  protected RemoteIteratorClient(RemoteIteratorServer<DataType> server,
                                 RemoteStreamExporter exporter)
    throws IOException
  {
    _remoteIStream = server.getRemoteInputStream(exporter);
  }

  public void setRemoteRetry(RemoteRetry newRemoteRetry)
  {
    _remoteRetry = newRemoteRetry;
  }
  
  /**
   * Sets up the communication pipeline and determines the initial state of
   * the iteration.
   */
  private void initialize()
    throws IOException
  {
    if(!_initialized) {
      try {
        _initialized = true;
        _hasNext = true;

        // setup communication (may throw EOFException if no data)
        _localIStream = RemoteInputStreamClient.wrap(_remoteIStream,
                                                     _remoteRetry);
        initialize(_localIStream);

        // read initial object
        doRead();
      
      } catch(EOFException e) {
        // empty iterator
        closeImpl();
      }
    }
  }

  /**
   * Attempts to read another object from the iteration.  Handles the
   * EOFException when there are no more objects left.
   */
  private void doRead()
    throws IOException
  {
    try {
      // read first object
      _nextObj = readNextObject();
    } catch(EOFException e) {
      // all done
      closeImpl();
    }
  }

  /**
   * Calls <code>closeIterator</code> if necessary.
   */
  private void closeImpl()
    throws IOException
  {
    // we only want to call closeIterator once, so we use the _hasNext flag to
    // determine if it has been called yet.
    if(_hasNext) {
      _hasNext = false;
      try {
        closeIterator();
      } finally {
        // make best effort close close remote stream even if data is horked
        if(_localIStream != null) {
          _localIStream.close();
        }
      }
    }
  }
    
  public boolean hasNext()
    throws IOException
  {
    initialize();
    return _hasNext;
  }

  public DataType next()
    throws IOException
  {
    if(!hasNext()) {
      throw new NoSuchElementException();
    }

    // hang onto current object
    DataType curObj = _nextObj;

    // read next object
    doRead();

    // return current object
    return curObj;
  }

  public void close()
    throws IOException
  {
    // we call initialize() so that we will be initialized if we have not been
    // already (so the remote stream will actually be closed).
    try {
      initialize();
    } finally {
      closeImpl();
    }
  }

  /**
   * Closes any resources held by this iterator.
   */
  protected void closeIterator()
    throws IOException
  {
    // nothing to do here
  }

  /**
   * Initializes the subclass with a handle to the underlying remote input
   * stream.  Will be called before any call to readNextObject().
   */
  protected abstract void initialize(InputStream istream)
    throws IOException;

  /**
   * Reads the next object from the underlying input stream and returns it.
   */
  protected abstract DataType readNextObject()
    throws IOException;

}
