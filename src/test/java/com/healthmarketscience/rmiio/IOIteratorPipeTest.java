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
