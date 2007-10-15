// Copyright (c) 2007 Health Market Science, Inc.

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
