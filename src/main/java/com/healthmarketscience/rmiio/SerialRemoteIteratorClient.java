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
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;

import com.healthmarketscience.rmiio.exporter.RemoteStreamExporter;


/**
 * Implementation of RemoteIteratorClient which uses java serialization to
 * receive objects from the RemoteIteratorServer.  Objects are deserialized
 * from the underlying remote input stream as needed and returned to the
 * ultimate consumer of the objects.
 * <p>
 * Note, the objects are read from the ObjectInputStream using the
 * {@link java.io.ObjectInputStream#readUnshared} method.  This is because
 * memory can build up in the ObjectInputStream over time and a large data set
 * can run the client and/or server out of memory.  In general, the objects
 * being iterated over most likely do not have shared references, so nothing
 * will be lost by this choice.  However, if shared references are desired,
 * the {@link #deserializeObject} method can be overriden by a custom subclass
 * to change this behavior.
 *
 * @author James Ahlborn
 */
public class SerialRemoteIteratorClient<DataType>
  extends RemoteIteratorClient<DataType>
  implements Serializable
{
  private static final long serialVersionUID = 3979308768398733924L;
  
  /** the output stream which does the java serialization work */
  private transient ObjectInputStream _objIStream;

  public SerialRemoteIteratorClient(
    RemoteIteratorServer<DataType> server)
    throws IOException
  {
    this(server, null);
  }
  
  public SerialRemoteIteratorClient(
    RemoteIteratorServer<DataType> server,
    RemoteStreamExporter exporter)
    throws IOException
  {
    super(server, exporter);
  }

  @Override
  protected void initialize(InputStream istream)
    throws IOException
  {
    _objIStream = new ObjectInputStream(istream);
  }

  @Override
  @SuppressWarnings("unchecked")
  protected DataType readNextObject()
    throws IOException
  {
    // read next object.  the deserializeObject method will throw the
    // necessary EOFException when no more data
    try {
      return (DataType)deserializeObject(_objIStream);
    } catch(ClassNotFoundException e) {
      throw (IOException)
        (new IOException()).initCause(e);
    }
  }

  @Override
  protected void closeIterator()
    throws IOException
  {
    if(_objIStream != null) {
      // close input stream
      _objIStream.close();
    }

    // close parent
    super.closeIterator();
  }

  /**
   * Reads the next object from the given input stream.  The default
   * implementation uses {@link java.io.ObjectInputStream#readUnshared}.
   * Subclasses may choose to change this behavior by overriding this method.
   *
   * @param istream the input stream from which to read the next object
   * @return the next object read
   */
  protected Object deserializeObject(ObjectInputStream istream)
    throws IOException, ClassNotFoundException
  {
    return istream.readUnshared();
  }
  
}
