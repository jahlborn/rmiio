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

/**
 * Useful almost concrete CloseableIOIterator implementation for the common
 * situation where you need to convert data from one iterator into data in
 * another iterator.  Everything in this class is implemented as expected, and
 * the {@link #convert} method is called on each element as it is returned
 * from the {@link #nextImpl} method.
 *
 * @author James Ahlborn
 */
public abstract class ConverterIOIterator<InType, OutType>
  extends AbstractCloseableIOIterator<OutType>
{
  /** input iterator */
  private final CloseableIOIterator<? extends InType> _iter;
  
  public ConverterIOIterator(CloseableIOIterator<? extends InType> iter) {
    _iter = iter;
  }

  public boolean hasNext() throws IOException {
    return _iter.hasNext();
  }

  @Override
  protected OutType nextImpl() throws IOException {
    return convert(_iter.next());
  }

  @Override
  protected void closeImpl() {
    RmiioUtil.closeQuietly(_iter);
  }

  /**
   * Converts from the input type to the output type.  Called by the
   * {@link #nextImpl} method.
   * @param in element to convert
   * @return the input value converted to the output type
   */
  protected abstract OutType convert(InType in) throws IOException;
  
}
