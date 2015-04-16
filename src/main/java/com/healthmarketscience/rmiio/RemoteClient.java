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
 * Interface common to most remote client implementations.
 *
 * @author James Ahlborn
 */
public interface RemoteClient {

  /** The default retry policy used if none is specified by the client. */
  public static final RemoteRetry DEFAULT_RETRY = RemoteRetry.SIMPLE;  
  
  /**
   * Sets the client side RemoteRetry policy to use for the underlying remote
   * communication layer.  For most client side implementations, this method
   * must be called before any other calls on this client object (any calls to
   * this method after one of those methods is called will have no affect).
   *
   * @param newRemoteRetry the new RemoteRetry policy to use for remote
   *                       communication.  {@code null} causes the
   *                       {@link #DEFAULT_RETRY} policy to be used.
   */
  public void setRemoteRetry(RemoteRetry newRemoteRetry);
  
}
