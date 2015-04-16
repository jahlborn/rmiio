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
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.io.InputStream;

import java.io.OutputStream;

/**
 * @author James Ahlborn
 */
public class BaseRemoteStreamTest extends TestCase
{

  private static final Log LOG = LogFactory.getLog(BaseRemoteStreamTest.class);
  
  protected List<Throwable> _clientExceptions = new ArrayList<Throwable>();
  protected List<AccumulateRemoteStreamMonitor<?>> _monitors =
    new ArrayList<AccumulateRemoteStreamMonitor<?>>();
  
  protected void checkMonitors(int numExpectedMonitors,
                               boolean expectClean)
    throws Exception
  {
    assertEquals(numExpectedMonitors, _monitors.size());
    for(AccumulateRemoteStreamMonitor<?> monitor : _monitors) {
      assertEquals(true, monitor._closed);
      assertEquals(expectClean, monitor._closedClean);
    }
  }

  protected void checkClientExceptions(int numExpectedExceptions)
  {
    if(numExpectedExceptions != _clientExceptions.size()) {
      for(Throwable t : _clientExceptions) {
        LOG.error("Client exceptions ", t);
      }
    }    
    assertEquals(numExpectedExceptions, _clientExceptions.size());
    for(Throwable t : _clientExceptions) {
      assertTrue(t instanceof IOException);
    }      
  }

  public static int cycleRead(InputStream in, byte[] tmp,
                              int iteration)
    throws IOException
  {
    switch(iteration % 3) {
    case 0:
      int b = in.read();
      if(b >= 0) {        
        tmp[0] = (byte)b;
        return 1;
      }
      return -1;

    case 1:
      return in.read(tmp);

    case 2:
      return in.read(tmp, 0, tmp.length);
      
    }
    throw new RuntimeException("should not get here");
  }
  
  public static void cycleWrite(OutputStream out, byte[] tmp,
                                int numBytes, int iteration)
    throws IOException
  {
    switch(iteration % 3) {
    case 0:
      for(int i = 0; i < numBytes; ++i) {
        out.write(tmp[i]);
      }
      return;

    case 1:
      if(numBytes == tmp.length) {
        out.write(tmp);
      } else {
        out.write(tmp, 0, numBytes);
      }
      return;

    case 2:
      int firstNumBytes = numBytes / 2;
      out.write(tmp, 0, firstNumBytes);
      int secondNumBytes = numBytes - firstNumBytes;
      if(secondNumBytes > 0) {
        out.write(tmp, firstNumBytes, secondNumBytes);
      }
      return;
      
    }
    throw new RuntimeException("should not get here");
  }

  
  public static class AccumulateRemoteStreamMonitor<S extends RemoteStreamServer>
    implements RemoteStreamMonitor<S>
  {
    public int _numWireBytes;
    public long _numSkippedWireBytes;
    public int _numWirePackets;
    public int _numLocalBytes;
    public long _numSkippedLocalBytes;
    public int _numReattempts = 0;
    public boolean _doAbort;
    public boolean _closed;
    public boolean _closedClean;

    public AccumulateRemoteStreamMonitor(boolean doAbort) {
      _doAbort = doAbort;
    }
      
    public void failure(S stream, Exception e)
    {
      LOG.debug("Transfer failed for " + stream + ": " + e);
    }
      
    public void bytesMoved(S stream, int numBytes, boolean isReattempt)
    {
      if(!isReattempt) {
        _numWireBytes += numBytes;
        _numWirePackets++;
      } else {
        _numReattempts++;
      }
    }

    public void bytesSkipped(S stream, long numBytes, boolean isReattempt)
    {
      if(!isReattempt) {
        _numSkippedWireBytes += numBytes;
        _numWirePackets++;
      } else {
        _numReattempts++;
      }
    }

    public void localBytesMoved(S stream, int numBytes)
    {
      _numLocalBytes += numBytes;
      if(_doAbort && (_numLocalBytes > 0)) {
        try {
          stream.abort();
        } catch(IOException e) {
          throw new RuntimeException(e);
        }
      }
    }

    public void localBytesSkipped(S stream, long numBytes)
    {
      _numSkippedLocalBytes += numBytes;
    }

    public void closed(S stream, boolean clean)
    {
      _closed = true;
      _closedClean = clean;
      LOG.debug("Transfer for " + stream + " finished: " + this);
    }

    @Override
    public String toString() {
      return "closed " + _closed +
        "; clean " + _closedClean +
        "; numWireBytes " + _numWireBytes +
        " in numPackets " + _numWirePackets +
        " (skipped " + _numSkippedWireBytes +
        "; reattempted " +
        _numReattempts + "); actual local bytes " +
        (_numLocalBytes + _numSkippedLocalBytes);
    }
  }
  
  
}
