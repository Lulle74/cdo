/**
 * Copyright (c) 2004 - 2009 Eike Stepper (Berlin, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eike Stepper - initial API and implementation
 *    Simon McDuff - http://bugs.eclipse.org/233490
 */
package org.eclipse.emf.cdo.internal.server;

import org.eclipse.emf.cdo.common.CDOCommonView;
import org.eclipse.emf.cdo.common.id.CDOID;
import org.eclipse.emf.cdo.server.IStoreAccessor;
import org.eclipse.emf.cdo.server.IView;
import org.eclipse.emf.cdo.server.StoreThreadLocal;
import org.eclipse.emf.cdo.spi.server.InternalRepository;
import org.eclipse.emf.cdo.spi.server.InternalSession;

import org.eclipse.net4j.util.StringUtil;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * @author Eike Stepper
 */
public class View implements IView
{
  private InternalSession session;

  private int viewID;

  private InternalRepository repository;

  private Set<CDOID> changeSubscriptionIDs = new HashSet<CDOID>();

  /**
   * @since 2.0
   */
  public View(InternalSession session, int viewID)
  {
    this.session = session;
    this.viewID = viewID;
    repository = session.getManager().getRepository();
  }

  public InternalSession getSession()
  {
    return session;
  }

  public int getViewID()
  {
    return viewID;
  }

  /**
   * @since 2.0
   */
  public Type getViewType()
  {
    return Type.READONLY;
  }

  /**
   * @since 2.0
   */
  public InternalRepository getRepository()
  {
    checkOpen();
    return repository;
  }

  /**
   * @since 2.0
   */
  public CDOID getResourceID(String path)
  {
    checkOpen();
    long timeStamp = getTimeStamp();
    CDOID resourceID = null;

    StringTokenizer tokenizer = new StringTokenizer(path, "/"); //$NON-NLS-1$
    while (tokenizer.hasMoreTokens())
    {
      String token = tokenizer.nextToken();
      if (!StringUtil.isEmpty(token))
      {
        resourceID = getResourceID(resourceID, token, timeStamp);
        if (resourceID == null)
        {
          return null;
        }
      }
    }

    return resourceID;
  }

  private CDOID getResourceID(CDOID folderID, String name, long timeStamp)
  {
    CDOID id = repository.getRevisionManager().getResourceID(folderID, name, timeStamp);
    if (id == null)
    {
      IStoreAccessor accessor = StoreThreadLocal.getAccessor();
      id = accessor.readResourceID(folderID, name, timeStamp);
    }

    return id;
  }

  /**
   * The timeStamp of the view ({@link CDOCommonView#UNSPECIFIED_DATE} if the view is an
   * {@link CDOCommonView.Type#AUDIT audit} view.
   * 
   * @since 2.0
   */
  public long getTimeStamp()
  {
    return UNSPECIFIED_DATE;
  }

  /**
   * @since 2.0
   */
  public synchronized void subscribe(CDOID id)
  {
    checkOpen();
    changeSubscriptionIDs.add(id);
  }

  /**
   * @since 2.0
   */
  public synchronized void unsubscribe(CDOID id)
  {
    checkOpen();
    changeSubscriptionIDs.remove(id);
  }

  /**
   * @since 2.0
   */
  public synchronized boolean hasSubscription(CDOID id)
  {
    checkOpen();
    return changeSubscriptionIDs.contains(id);
  }

  /**
   * @since 2.0
   */
  public synchronized void clearChangeSubscription()
  {
    checkOpen();
    changeSubscriptionIDs.clear();
  }

  @Override
  public String toString()
  {
    return MessageFormat.format("View[{0}]", viewID); //$NON-NLS-1$
  }

  /**
   * @since 2.0
   */
  public void close()
  {
    if (!isClosed())
    {
      session.viewClosed(this);
    }
  }

  /**
   * @since 2.0
   */
  public void doClose()
  {
    clearChangeSubscription();
    session = null;
    repository = null;
    changeSubscriptionIDs = null;
  }

  /**
   * @since 2.0
   */
  public boolean isClosed()
  {
    return session == null;
  }

  private void checkOpen()
  {
    if (isClosed())
    {
      throw new IllegalStateException("View closed"); //$NON-NLS-1$
    }
  }
}
