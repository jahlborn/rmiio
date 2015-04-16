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
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Remote interface which allows exporting an InputStream-like interface over
 * the network.  When combined with the {@link RemoteInputStreamServer}
 * subclasses on the server side and the {@link RemoteInputStreamClient} on
 * the client side, this class provides a true remote InputStream (in other
 * words, should never be used alone, use the utility classes).
 * <p>
 * Note that all methods on this interface are idempotent (when used
 * correctly), and can therefore be retried as necessary in the face of
 * RemoteExceptions.
 * <p>
 * An actual instance of this class is not intended for use by more than one
 * client, and should be treated in a similar manner to an "un-synchronized"
 * local interface.
 *
 * @author James Ahlborn
 */
public interface RemoteInputStream extends Remote
{

  /**
   * Returns <code>true</code> if the stream is using GZIP compression over
   * the wire.
   *
   * @return <code>true</code> iff the stream data is compressed,
   *         <code>false</code> otherwise
   */
  public boolean usingGZIPCompression()
    throws IOException, RemoteException;
  
  /**
   * Returns the number of bytes that can be read from this stream without
   * blocking.  Note that this is an <b>approximate</b> number and should be
   * treated as such.
   *
   * @return the number of bytes that can be read without blocking
   */
  public int available()
    throws IOException, RemoteException;

  /**
   * Closes the input stream and releases the resources for this server
   * object.  Note that the remote object <i>may no longer be accessible</i>
   * after this call (depending on the implementation), so clients should not
   * attempt to use this stream after making this call.
   *
   * @param readSuccess <code>true</code> iff all data was read successfully
   *                    by the client, <code>false</code> otherwise
   */
  public void close(boolean readSuccess)
    throws IOException, RemoteException;

  /**
   * Reads the next chunk of data for this stream.  The amount of data
   * returned is up to the underlying implementation.
   *
   * The given packetId parameter (if used correctly) allows this operation to
   * be idempotent.  This parameter must be a monotonically increasing,
   * positive integer.  If the client fails to receive a given packet, it may
   * reattempt to retrieve the same packet by giving the same packetId as from
   * the failed call.  However, only the current packet may be reattempted
   * (the client cannot attempt to retrieve any other previous packets).  When
   * requesting a new packet, the caller does not need to give a sequential
   * id, just a greater one (hence the term monotonically increasing).
   *
   * @param packetId client specified id for this packet
   * @return iff the packetId was the same one from the last read call,
   *         returns the last read chunk of data.  Otherwise, reads and
   *         returns a new chunk of data.
   */
  public byte[] readPacket(int packetId)
    throws IOException, RemoteException;

  /**
   * Skips and discards up to the given number of bytes in the stream, and
   * returns the actual number of bytes skipped.  This method is not allowed
   * to be called if using compression on the wire (because of the various
   * layers of buffering).
   *
   * The given skipId parameter (if used correctly) allows this operation to
   * be idempotent.  This parameter must be a monotonically increasing,
   * positive integer.  If the client fails to receive the return from a skip
   * call, it may reattempt the same skip call by giving the same skipId as
   * from the failed call.  However, only the current skip may be reattempted
   * (the client cannot reattempt previous skips).  When attempting a new skip
   * call, the caller does not need to give a sequential id, just a greater
   * one (hence the term monotonically increasing).
   *
   * @param n the number of bytes to skip
   * @param skipId client specified id for this skip attempt
   * @return iff the skipId was the same one from the last skip call, returns
   *         the result of the last skip call.  Otherwise, skips up to the
   *         given number of bytes and returns the actual number of bytes
   *         skipped.
   */
  public long skip(long n, int skipId)
    throws IOException, RemoteException;
  
}
