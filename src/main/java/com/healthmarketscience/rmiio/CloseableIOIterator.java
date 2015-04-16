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
import java.util.Iterator;

/**
 * Convenience interface which combines IOIterator and Closeable.
 *
 * @author James Ahlborn
 */
public interface CloseableIOIterator<DataType> extends IOIterator<DataType>,
                                                       Closeable
{

  /**
   * Utility class for using a normal Iterator as a CloseableIOIterator.
   * @deprecated use {@link RmiioUtil#adapt} instead
   */
  @Deprecated
  public static class Adapter<DataType>
    implements CloseableIOIterator<DataType>
  {
    private final Iterator<? extends DataType> _iter;

    public Adapter(Iterator<? extends DataType> iter) {
      _iter = iter;
    }

    public boolean hasNext() { return _iter.hasNext(); }

    public DataType next() { return _iter.next(); }

    public void close() { }
  }
    
}
