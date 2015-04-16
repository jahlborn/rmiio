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

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Utility methods for working with rmiio classes.
 *
 * @author James Ahlborn
 */
public class RmiioUtil
{
  private static final Log LOG = LogFactory.getLog(RmiioUtil.class);
  
  private RmiioUtil() {
  }

  /**
   * Adapts an {@link Iterator} to the {@link CloseableIOIterator} interface.
   * If the given iterator implements {@link Closeable}, it will be closed by
   * a close call on the wrapper.  The wrapper implementation is a subclass of
   * {@link AbstractCloseableIOIterator}, so the iterator will automagically
   * be closed if used with a SerialRemoteIteratorServer.
   */
  public static <T> CloseableIOIterator<T> adapt(Iterator<? extends T> iter)
  {
    if(iter == null) {
      return null;
    }
    return new IOIteratorAdapter<T>(iter);
  }

  /**
   * Closes the given Closeable if non-{@code null}, swallowing any
   * IOExceptions generated.
   */
  public static void closeQuietly(Closeable closeable)
  {
    // yes, this has been written many times before and elsewhere, but i did
    // not want to add a dependency just for one method
    if(closeable != null) {
      try {
        closeable.close();
      } catch(IOException e) {
        // optionally log the exception, but otherwise ignore
        if(LOG.isDebugEnabled()) {
          LOG.debug("Failed closing closeable", e);
        }
      }
    }
  }
  
  /**
   * Adapts an Iterator to the CloseableIOIterator interface.
   */
  private static class IOIteratorAdapter<DataType>
    extends AbstractCloseableIOIterator<DataType>
  {
    private final Iterator<? extends DataType> _iter;

    public IOIteratorAdapter(Iterator<? extends DataType> iter) {
      _iter = iter;
    }

    public boolean hasNext() {
      return _iter.hasNext();
    }

    @Override
    protected DataType nextImpl() {
      return _iter.next();
    }

    @Override
    protected void closeImpl() {
      if(_iter instanceof Closeable) {
        closeQuietly((Closeable)_iter);
      }
    }
    
  }
  
}
