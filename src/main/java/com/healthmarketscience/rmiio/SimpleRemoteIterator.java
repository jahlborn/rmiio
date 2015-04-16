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

import java.io.Serializable;
import java.util.Iterator;

/**
 * Trivial implementation of RemoteIterator for small collections.  The given
 * Iterable must be Serializable.
 *
 * @author James Ahlborn
 */
public class SimpleRemoteIterator<DataType>
  implements RemoteIterator<DataType>, Serializable
{
  private static final long serialVersionUID = -4737864032220987188L;

  /** the serializable collection */
  private final Iterable<DataType> _iterable;
  /** the current iterator for said collection */
  private transient Iterator<DataType> _iter;

  public SimpleRemoteIterator(Iterable<DataType> iterable)
  {
    if(iterable == null) {
      throw new IllegalArgumentException("Iterable cannot be null");
    }
    _iterable = iterable;
  }

  private void init() {
    // initialize the iterator reference if necessary
    if(_iter == null) {
      _iter = _iterable.iterator();
    }
  }
  
  public boolean hasNext()
  {
    init();
    return _iter.hasNext();
  }

  public DataType next()
  {
    init();
    return _iter.next();
  }
  
  public void close() {
    // nothing to do
  }

  public void setRemoteRetry(RemoteRetry newRemoteRetry) {
    // ignored
  }
  
}
