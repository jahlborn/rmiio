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


import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Formatter;

import junit.framework.TestCase;

/**
 * Unit tests for LineIterator
 *
 * @author Mike DeLaurentis
 */
public class LineIteratorTest extends TestCase {

  /** 
   * Input string for all tests 
   */
  private static final String TEST_INPUT = new Formatter()
    .format("a  %n")
    .format("%n")
    .format(" b %n")
    .format("   %n")
    .format("  c%n")
    .toString();

  /** 
   * Returns a {@link BufferedReader} that reads lines from TEST_INPUT 
   */
  private static BufferedReader getReader() {
    return new BufferedReader(new StringReader(TEST_INPUT));
  }

  public void testNoTrimNoSkip() throws IOException {
    LineIterator it = new LineIterator(getReader(), false, false);
    try {
      assertEquals("a  ", it.next());
      assertEquals("", it.next());
      assertEquals(" b ", it.next());
      assertEquals("   ", it.next());
      assertEquals("  c", it.next());
    } finally {
      RmiioUtil.closeQuietly(it);
    }    
  }

  public void testTrimNoSkip() throws IOException {
    LineIterator it = new LineIterator(getReader(), true, false);
    try {
      assertEquals("a", it.next());
      assertEquals("", it.next());
      assertEquals("b", it.next());
      assertEquals("", it.next());
      assertEquals("c", it.next());
    } finally {
      RmiioUtil.closeQuietly(it);
    }    
  }

  public void testNoTrimSkip() throws IOException {
    LineIterator it = new LineIterator(getReader(), false, true);
    try {
      assertEquals("a  ", it.next());
      assertEquals(" b ", it.next());
      assertEquals("   ", it.next());
      assertEquals("  c", it.next());
    } finally {
      RmiioUtil.closeQuietly(it);
    }    
  }

  public void testTrimSkip() throws IOException {
    LineIterator it = new LineIterator(getReader(), true, true);
    try {
      assertEquals("a", it.next());
      assertEquals("b", it.next());
      assertEquals("c", it.next());
    } finally {
      RmiioUtil.closeQuietly(it);
    }    
  }

}
