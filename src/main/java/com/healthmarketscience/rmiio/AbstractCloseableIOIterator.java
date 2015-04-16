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
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Convenience base class for CloseableIOIterator implementations, especially
 * suited for use as the local iterator for a RemoteIteratorServer
 * instance.  This implementation manages the closing of the local resources
 * through three separate mechanisms.  The {@link #close} method will be
 * called:
 * <ul>
 * <li>by the {@link #next} method when the {@link #hasNext} method starts
 *     returning {@code false}</li>
 * <li>when the close method is called directly (duh)</li>
 * <li>if used with a RemoteIteratorServer, when the server is shutdown</li>
 * </ul>
 * This three-pronged attack provides a pretty strong guarantee that the local
 * resources will be closed at some point in time.  Note that the
 * implementation of the {@link #close} method will call the
 * {@link #closeImpl} method at most once.  Extraneous invocations will be
 * ignored.
 *
 * @author James Ahlborn
 */
public abstract class AbstractCloseableIOIterator<DataType>
  implements CloseableIOIterator<DataType>
{

  /** value which guarantees that the {@link #closeImpl} method is called at
      most once */
  private final AtomicBoolean _closed = new AtomicBoolean();
  
  public AbstractCloseableIOIterator() {
  }

  public final DataType next()
    throws IOException
  {
    if(!hasNext()) {
      throw new NoSuchElementException();
    }
    // grab current element
    DataType next = nextImpl();
    if(!hasNext()) {
      // all done with local resources, close them
      close();
    }
    // return current element
    return next;
  }
  
  public final void close()
  {
    // only close once
    if(_closed.compareAndSet(false, true)) {
      closeImpl();
    }
  }

  /**
   * Does the actual work of the {@link #next} method.  Will only be called if
   * {@link #hasNext} is currently returning {@code true}.
   */
  protected abstract DataType nextImpl() throws IOException;

  /**
   * Does the actual closing of the local resources.  Will be called at most
   * once by the {@link #close} method regardless of how many times that
   * method is invoked.
   * <p>
   * Note, this method does not throw {@code IOException} because it can be
   * called in a variety of different scenarios and throwing an IOException
   * would be useless in many of them (and often, failure to close is merely a
   * nuisance, not a cause for failure).
   */
  protected abstract void closeImpl();
  
}
