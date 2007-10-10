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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

/**
 * @author James Ahlborn
 */
public class IOIteratorPipeTest extends TestCase {


  private int _sinkAbortNum = -1;
  private int _sourceAbortNum = -1;
  private int _queueSize = 2;
  private List<Object> _srcList;
  private List<Object> _destList = new ArrayList<Object>();
  private List<Throwable> _sinkErrors = new ArrayList<Throwable>();
  private List<Throwable> _sourceErrors = new ArrayList<Throwable>();

  private IOIteratorPipe<Object> _pipe;
  
  public IOIteratorPipeTest(String name) {
    super(name);
  }

  public void testEmpty()
    throws Exception
  {
    _srcList = Collections.emptyList();

    doTest();

    assertEquals(_srcList, _destList);
    assertEquals(0, _sinkErrors.size());
    assertEquals(0, _sourceErrors.size());
  }

  public void testSimple()
    throws Exception
  {
    _srcList = Arrays.asList(new Object(), new Object(), new Object(),
                             new Object(), new Object(), new Object());

    doTest();

    assertEquals(_srcList, _destList);
    assertEquals(0, _sinkErrors.size());
    assertEquals(0, _sourceErrors.size());
  }

  public void testSinkAbort()
    throws Exception
  {
    _srcList = Arrays.asList(new Object(), new Object(), new Object(),
                             new Object(), new Object(), new Object());
    _sinkAbortNum = 2;

    doTest();

    assertTrue(_sinkAbortNum >= _destList.size());
    assertEquals(1, _sourceErrors.size());
    assertEquals(0, _sinkErrors.size());
  }

  public void testImmediateSinkAbort()
    throws Exception
  {
    _srcList = Arrays.asList(new Object(), new Object(), new Object(),
                             new Object(), new Object(), new Object());
    _sinkAbortNum = 0;

    doTest();

    assertTrue(_sinkAbortNum >= _destList.size());
    assertEquals(1, _sourceErrors.size());
    assertEquals(0, _sinkErrors.size());
  }

  public void testSourceAbort()
    throws Exception
  {
    _srcList = Arrays.asList(new Object(), new Object(), new Object(),
                             new Object(), new Object(), new Object());
    _sourceAbortNum = 2;

    doTest();

    assertEquals(_sourceAbortNum, _destList.size());
    assertEquals(0, _sourceErrors.size());
    assertEquals(1, _sinkErrors.size());
  }

  public void testImmediateSourceAbort()
    throws Exception
  {
    _srcList = Arrays.asList(new Object(), new Object(), new Object(),
                             new Object(), new Object(), new Object());
    _sourceAbortNum = 0;

    doTest();

    assertEquals(_sourceAbortNum, _destList.size());
    assertEquals(0, _sourceErrors.size());
    assertEquals(1, _sinkErrors.size());
  }

  
  private void doTest()
    throws Exception
  {
    _pipe = new IOIteratorPipe<Object>(_queueSize);

    Thread t = new Thread(new Sourcer(), "Sourcer");
    t.start();
    
    try {
      boolean aborted = false;
      for(int i = 0; i < _srcList.size(); ++i) {
        if(i == _sinkAbortNum) {
          aborted = true;
          break;
        }
        _pipe.getSink().addNext(_srcList.get(i));
      }
      if(!aborted) {
        _pipe.getSink().setFinished();
      }
    } catch(IOException e) {
      _sinkErrors.add(e);
    } finally {
      if(_pipe.getSink() != null) {
        try {
          _pipe.getSink().close();
        } catch(IOException e) {
          // ignore
        }
      }
    }

    t.join();
  }

  private class Sourcer implements Runnable
  {
    public void run() {
      try {
        while(_pipe.getSource().hasNext()) {
          if(_destList.size() == _sourceAbortNum) {
            break;
          }
          _destList.add(_pipe.getSource().next());
        }
      } catch(IOException e) {
        _sourceErrors.add(e);
      } finally {
        if(_pipe.getSource() != null) {
          _pipe.getSource().close();
        }
      }
    }
  }
  
}
