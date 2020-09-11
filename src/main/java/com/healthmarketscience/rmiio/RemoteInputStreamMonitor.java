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
 * Implementation of RemoteStreamMonitor for RemoteInputStreamServers which
 * does nothing.
 *
 * @author James Ahlborn
 */
public class RemoteInputStreamMonitor
  implements RemoteStreamMonitor<RemoteInputStreamServer>
{
  public RemoteInputStreamMonitor() {}

  @Override
  public void failure(RemoteInputStreamServer stream, Exception e) {}

  @Override
  public void bytesMoved(RemoteInputStreamServer stream,
                         int numBytes, boolean isReattempt) {}

  @Override
  public void bytesSkipped(RemoteInputStreamServer stream,
                           long numBytes, boolean isReattempt) {}

  @Override
  public void localBytesMoved(RemoteInputStreamServer stream, int numBytes) {}

  @Override
  public void localBytesSkipped(RemoteInputStreamServer stream,
                                long numBytes) {}

  @Override
  public void closed(RemoteInputStreamServer stream,
                     boolean clean) {}

}
