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

package com.healthmarketscience.rmiio.exporter;

import java.rmi.RemoteException;

import com.healthmarketscience.rmiio.RemoteStreamServer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Base class for objects which manage exporting RemoteStreamServers.
 * "Exporting" is the act of making a RemoteStreamServer available remotely
 * via some RPC framework (such as RMI).  This class allows the rmiio
 * utilities to be used with any RPC framework by separating the server
 * implementation from the RPC integration.
 * <p>
 * See the {@link #getInstance} method for details on how the default exporter
 * can be specified as a system property.
 * <p>
 * RemoteStreamExporter implementations are expected to be thread-safe and
 * reentrant after construction.
 * <p>
 * For some helper classes which may be useful for alternative RPC
 * frameworks, see {@link RemoteStreamServerInvokerHelper},
 * {@link RemoteInputStreamClientProxy}, and
 * {@link RemoteOutputStreamClientProxy}.
 *
 * @author James Ahlborn
 */
public abstract class RemoteStreamExporter
{
  protected static final Log LOG = LogFactory.getLog(RemoteStreamExporter.class);  

  /** system property used by {@link #getInstance} to determine which exporter
      implementation to return */
  public static final String EXPORTER_PROPERTY =
    "com.healthmarketscience.rmiio.exporter";

  /** name of the default exporter implementation returned by
      {@link #getInstance} if none is specified via system property */
  public static final String DEFAULT_EXPORTER_CLASS_NAME =
    DefaultRemoteStreamExporter.class.getName();

  /** RemoteStreamExporter instance returned by {@link #getInstance}, created
      once, on demand */
  private static RemoteStreamExporter _INSTANCE = null;
  
  protected RemoteStreamExporter() {
  }

  /**
   * Returns the default RemoteStreamExporter to use.
   * @return the default RemoteStreamExporter to use, either specified by the
   *         system property {@link #EXPORTER_PROPERTY} or an instance of
   *         {@link #DEFAULT_EXPORTER_CLASS_NAME}.  The exporter is
   *         instantiated once, on demand, and returned thereafter.
   */
  public static synchronized RemoteStreamExporter getInstance() {
    if(_INSTANCE == null) {
      String exporterClassName = System.getProperty(
          EXPORTER_PROPERTY, DEFAULT_EXPORTER_CLASS_NAME);
      LOG.info("Using stream exporter " + exporterClassName);
      try {
        // first attempt to load from this class' classloader
        Class<?> clazz = null;
        Exception e = null;
        try {
          clazz = Class.forName(exporterClassName);
        } catch(ClassNotFoundException cnfe) {
          // if not found, try context classloader if available
          e = cnfe;
          ClassLoader ccl = Thread.currentThread().getContextClassLoader();
          if(ccl != null) {
            clazz = Class.forName(exporterClassName, true, ccl);
          }
        }
        if(clazz != null) {
          _INSTANCE = (RemoteStreamExporter)clazz.newInstance();
        } else {
          throw e;
        } 
      } catch(Exception e) {
        throw new IllegalArgumentException(
            "could not instantiate exporter " + exporterClassName, e);
      }
    }
    return _INSTANCE;
  }

  /**
   * Exports the given stream server via the desired RPC framework and returns
   * the "remote" instance (often some sort of serializable stub object).  The
   * given stream instance should now be reachable from a remote call.
   * @return the remote stub used for interacting with this stream instance
   *         from a remote client
   * @throws RemoteException if the stream instance could not be exported
   */
  public <StreamType,
          StreamServerType extends RemoteStreamServer<?,StreamType>> StreamType export(
      StreamServerType server)
    throws RemoteException
  {
    synchronized(server) {

      if(LOG.isDebugEnabled()) {
        LOG.debug("Exporting remote object " + server);
      }

      // first, do the actual export (if the exportImpl call fails, we have to
      // assume the object was not successufully exported)
      Object stubObj = exportImpl(server);

      boolean exportProcessed = false;
      StreamType stub = null;
      try {

        // cast the stub to the correct type
        stub = server.getRemoteClass().cast(stubObj);
        
        // let the stream do stuff if necessary
        server.exported(this);

        // all good!
        exportProcessed = true;
        
      } finally {
        if(!exportProcessed) {
          // bailout!
          unexport(server);
        }
      }

      return stub;
    }
  }
  
  /**
   * Unexports the given previously exported stream server from the desired
   * RPC framework.  The given stream instance will no longer be reachable
   * from a remote call.
   */
  public void unexport(RemoteStreamServer<?,?> server)
  {
    synchronized(server) {
      try {
        if(LOG.isDebugEnabled()) {
          LOG.debug("Unexporting remote object " + server);
        }

        // do the actual unexport
        unexportImpl(server);
        
      } catch(Exception e) {
        if(LOG.isDebugEnabled()) {
          LOG.debug("Unexporting failed! for " + server, e);
        }
        // whatever...
      }
    }
  }

  /**
   * Called by {@link #export} to do the actual export work for the relevant
   * RPC framework.  This method will be called synchronized on the given
   * stream instance, so it will not overlap an {@link #unexport} call for the
   * same instance.
   * <p>
   * Note, RemoteStreamServer implements Unreferenced, which is an rmi
   * interface used to clean up servers which have lost their clients.  RPC
   * frameworks which export remote streams should attempt to handle abnormal
   * client termination, and are encouraged to make use of the Unreferenced
   * interface to shutdown an orphaned stream server.
   * 
   * @return the remote stub, which should be an instance of the remote
   *         interface of this server
   * @throws RemoteException if the stream instance could not be exported
   */
  protected abstract Object exportImpl(RemoteStreamServer<?,?> server)
    throws RemoteException;

  /**
   * Called by {@link #unexport} to do the actual unexport work for the
   * relevant RPC framework.  This method will be called synchronized on the
   * given stream instance, so it will not overlap an {@link #export} call for
   * the same instance.  This method call is allowed break existing
   * connections to this stream instance.  Any exceptions thrown will be
   * logged, but otherwise ignored.
   * @throws Exception if the unexport failed
   */
  protected abstract void unexportImpl(RemoteStreamServer<?,?> server)
    throws Exception;
  
}
