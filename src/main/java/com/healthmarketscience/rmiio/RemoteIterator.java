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
import java.io.Serializable;


/**
 * Interface for streaming a read-only collection of objects using rmi.
 * Interface mimics the Iterator interface, but allows for IOExceptions and
 * requires the implementation to be Serializable.  This interface is useful
 * for situations where a collection is too large to be in memory all at once
 * and instead needs to be streamed to the consumer of the data.  For example,
 * data could be read directly from a database and streamed to a remote object
 * using this utility.  Generally, one would use the
 * RemoteIteratorClient/Server classes to implement the remote functionality.
 * <p>
 * Since this interface is built for use in a remote fashion, there is also a
 * close() method to facilitate better resource management.  Consumers of the
 * iterator should ensure that the close method is called one way or another
 * (especially if the entire iteration is not consumed!), or resources may
 * not be utilized as efficiently on the server.
 * <p>
 * Note, implementations of this class are not required to be thread-safe.
 *
 * @author James Ahlborn
 */
public interface RemoteIterator<DataType>
  extends CloseableIOIterator<DataType>, Serializable, RemoteClient
{

  /**
   * Closes the iterator and releases the resources for the server
   * object.  Note that the remote object <i>may no longer be accessible</i>
   * after this call (depending on the implementation), so clients should not
   * attempt to use this iterator after making this call.
   */
  public void close() throws IOException;

}
