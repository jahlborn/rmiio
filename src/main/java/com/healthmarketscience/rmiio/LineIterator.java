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
import static com.healthmarketscience.rmiio.RmiioUtil.closeQuietly;

/**
 * CloseableIOIterator that reads lines from a BufferedReader, 
 * optionally trimming whitespace and/or skipping blank lines.
 *
 * @author James Ahlborn
 */
public class LineIterator extends AbstractCloseableIOIterator<String> {

  /** 
   * We'll read lines from this reader
   */
  private final BufferedReader _reader;

  /**
   * The line to be returned by the next call to next()
   */
  private String _next;

  /**
   * If true, we'll trim() the lines before emitting them.
   */
  private boolean _trimWhitespace;

  /**
   * If true, we'll skip over blank lines.
   */
  private boolean _skipBlankLines;

  /**
   * Creates a new <code>LineIterator</code> instance that will
   * read lines from the given {@link BufferedReader} and return them from
   * calls to next().
   *
   * @param reader the source of lines for this iterator
   * @param trimWhitespace if true, leading and trailing whitespace will be
   *                       trimmed from each line
   * @param skipBlankLines if true, empty lines will be skipped
   */
  public LineIterator(BufferedReader reader, 
                      boolean trimWhitespace,
                      boolean skipBlankLines)
    throws IOException
  {
    _reader = reader;
    _skipBlankLines = skipBlankLines;
    _trimWhitespace = trimWhitespace;
    getNext();
  }

  private void getNext() throws IOException
  {
    while(true) {
      _next = _reader.readLine();
      if(_next != null) {
        if (_trimWhitespace) {
          _next = _next.trim();
        }
        if(_skipBlankLines && _next.length() == 0) {
          continue;
        }
      }
      return;
    }
  }

  public boolean hasNext() throws IOException
  {
    return(_next != null);
  }

  @Override
  protected String nextImpl() throws IOException
  {
    String cur = _next;
    getNext();
    return cur;
  }

  @Override
  protected void closeImpl() {
    closeQuietly(_reader);
  }

}
