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
package examples.iiop;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

import com.healthmarketscience.rmiio.RemoteInputStream;

/**
 * A simple Remote interface for an RMI server which consumes a
 * RemoteInputStream.
 *
 * @author James Ahlborn
 */
public interface RemoteFileServer extends Remote
{

  public void sendFile(RemoteInputStream ristream)
    throws RemoteException, IOException;

}
