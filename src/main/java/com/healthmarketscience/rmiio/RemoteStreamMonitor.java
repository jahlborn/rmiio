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
 * Interface for monitoring the progress of a remote stream, such as
 * {@link RemoteInputStream} or {@link RemoteOutputStream}.
 *
 * @author James Ahlborn
 */
public interface RemoteStreamMonitor<StreamServerType> {

  /**
   * Called when an IOException is thrown by one of the stream methods.
   *
   * @param stream the stream on which the exception was thrown
   * @param e the thrown exception
   */
  public void failure(StreamServerType stream, Exception e);

  /**
   * Called when some bytes are transferred over the wire by the given stream.
   *
   * Note, as some streams use compression over the wire, the total number of
   * bytes moved/skipped may not equal the total number of <i>local</i> bytes
   * moved/skipped.
   *
   * @param stream the stream for which the bytes are being transferred
   * @param numBytes number of bytes transferred
   * @param isReattempt indicates if this is the first attempt
   *                    (<code>false</code>) or a subsequent attempt
   *                    (<code>true</code>)
   */
  public void bytesMoved(StreamServerType stream, int numBytes, boolean isReattempt);

  /**
   * Called when some bytes are skipped for transfer over the wire by the
   * given stream.  Will not be called for output streams.
   *
   * Note, as some streams use compression over the wire, the total number of
   * bytes moved/skipped may not equal the total number of <i>local</i> bytes
   * moved/skipped.
   *
   * @param stream the stream for which the bytes are being skipped
   * @param numBytes number of actual bytes skipped
   * @param isReattempt indicates if this is the first attempt
   *                    (<code>false</code>) or a subsequent attempt
   *                    (<code>true</code>)
   */
  public void bytesSkipped(StreamServerType stream, long numBytes,
                           boolean isReattempt);

  /**
   * Called when some bytes are moved to/from the local stream.
   *
   * Note, as some streams use compression over the wire, the total number of
   * bytes moved/skipped may not equal the total number of <i>local</i> bytes
   * moved/skipped.
   *
   * @param stream the remote stream for which the bytes are being moved
   * @param numBytes number of bytes moved
   */
  public void localBytesMoved(StreamServerType stream, int numBytes);

  /**
   * Called when some bytes from the local stream are skipped.  Will not be
   * called for output streams.
   *
   * Note, as some streams use compression over the wire, the total number of
   * bytes moved/skipped may not equal the total number of <i>local</i> bytes
   * moved/skipped.
   *
   * @param stream the stream for which the bytes are being skipped
   * @param numBytes number of actual bytes skipped
   */
  public void localBytesSkipped(StreamServerType stream, long numBytes);

  /**
   * Called when the given stream is closed.  The clean parameter indicates
   * whether or not the transfer completed successfully.
   *
   * @param stream the stream for which the bytes are being skipped
   * @param clean <code>true</code> iff all data was sent successfully over
   *              the wire and the stream was closed, <code>false</code>
   *              otherwise.
   */
  public void closed(StreamServerType stream, boolean clean);
  
}
