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
import java.util.Iterator;



/**
 * Interface which mimics the Iterator interface but allows IOExceptions to be
 * thrown by the implementation.
 * <p>
 * If the implementation needs to implement some sort of cleanup operation,
 * implement {@link CloseableIOIterator} instead.
 *
 * @author James Ahlborn
 */
public interface IOIterator<DataType>
{

  /**
   * Returns <code>true</code> iff the iteration has more elements.
   */
  public boolean hasNext() throws IOException;

  /**
   * Returns the next element in the iteration.
   */
  public DataType next() throws IOException;



  /**
   * Utility class for using a normal Iterator as an IOIterator.
   * @deprecated use {@link RmiioUtil#adapt} instead
   */
  @Deprecated
  public static class Adapter<DataType>
    implements IOIterator<DataType>
  {
    private final Iterator<? extends DataType> _iter;

    public Adapter(Iterator<? extends DataType> iter) {
      _iter = iter;
    }

    public boolean hasNext() { return _iter.hasNext(); }

    public DataType next() { return _iter.next(); }

  }
  
}
