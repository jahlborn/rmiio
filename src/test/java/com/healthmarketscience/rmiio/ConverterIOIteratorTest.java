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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import junit.framework.TestCase;

/**
 * @author James Ahlborn
 */
public class ConverterIOIteratorTest extends TestCase {

  public ConverterIOIteratorTest(String name) {
    super(name);
  }
  
  public void testConverter() throws Exception
  {
    final AtomicBoolean closed = new AtomicBoolean();
    List<String> input = Arrays.asList("13", "21", "-3");
    CloseableIOIterator<String> inputIter =
      new SimpleRemoteIterator<String>(input) {
        private static final long serialVersionUID = 0L;
        @Override
        public void close() {
          super.close();
          closed.set(true);
        }
      };
    List<Integer> output = new ArrayList<Integer>();

    CloseableIOIterator<Integer> iter = null;
    for(iter =
          new ConverterIOIterator<String, Integer>(inputIter) {
            @Override
            protected Integer convert(String in) {
              return Integer.valueOf(in);
            }
          }; iter.hasNext(); )
    {
      output.add(iter.next());
    }
    iter.close();

    assertTrue(closed.get());
    assertEquals(Arrays.asList(13, 21, -3), output);
  }

}
