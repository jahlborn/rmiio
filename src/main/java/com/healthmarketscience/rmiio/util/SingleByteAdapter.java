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

package com.healthmarketscience.rmiio.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Adapts a single byte read/write call to the corresponding call to a byte
 * array read/write call. Yeah, I know it seems trivial, but I use it
 * everywhere I implement an InputStream or OutputStream.  This class is not
 * synchronized.
 *
 * @author James Ahlborn
 */
public final class SingleByteAdapter {

  /** buffer for single byte read/write calls */
  private final byte[] _tmpBuf = new byte[1];
  
  /**
   * Calls {@link OutputStream#write(byte[],int,int)} on the given
   * OutputStream using an internal buffer with the given byte written to it.
   */
  public void write(int b, OutputStream ostream) throws IOException
  {
    _tmpBuf[0] = (byte)b;
    ostream.write(_tmpBuf, 0, 1);
  }

  /**
   * Calls {@link InputStream#read(byte[],int,int)} on the given InputStream
   * using an internal buffer, and returns the relevant result (either the end
   * of stream flag or the byte that was read).
   */
  public int read(InputStream istream) throws IOException
  {
    int numRead = istream.read(_tmpBuf, 0, 1);
    if(numRead < 0) {
      return numRead;
    }
      
    // we have to use the 'bitwise and' here so that the byte doesn't get
    // sign extended into an int, thus changing the actual value returned to
    // the caller.
    return _tmpBuf[0] & 0xff;
  }
  
}
