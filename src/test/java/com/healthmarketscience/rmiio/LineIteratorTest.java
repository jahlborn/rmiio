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
