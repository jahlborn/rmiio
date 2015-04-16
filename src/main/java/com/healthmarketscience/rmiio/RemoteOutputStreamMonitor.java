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



/**
 * Implementation of RemoteStreamMonitor for RemoteOutputStreamServers which
 * does nothing.
 *
 * @author James Ahlborn
 */
public class RemoteOutputStreamMonitor
  implements RemoteStreamMonitor<RemoteOutputStreamServer>
{
  public RemoteOutputStreamMonitor() {
    
  }

  public void failure(RemoteOutputStreamServer stream, Exception e) {}

  public void bytesMoved(RemoteOutputStreamServer stream,
                         int numBytes, boolean isReattempt) {}

  public void bytesSkipped(RemoteOutputStreamServer stream,
                           long numBytes, boolean isReattempt) {
    // should never be called for output streams
    throw new UnsupportedOperationException();
  }

  public void localBytesMoved(RemoteOutputStreamServer stream,
                              int numBytes) {}

  public void localBytesSkipped(RemoteOutputStreamServer stream,
                                long numBytes) {
    // should never be called for output streams
    throw new UnsupportedOperationException();
  }
  
  public void closed(RemoteOutputStreamServer stream,
                     boolean clean) {}
  
}
