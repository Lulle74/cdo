/**
 * Copyright (c) 2004 - 2009 Eike Stepper (Berlin, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eike Stepper - initial API and implementation
 */
package org.eclipse.emf.cdo.spi.server;

import org.eclipse.emf.cdo.common.id.CDOID;
import org.eclipse.emf.cdo.common.id.CDOIDAndVersion;
import org.eclipse.emf.cdo.common.model.CDOPackageUnit;
import org.eclipse.emf.cdo.common.revision.delta.CDORevisionDelta;
import org.eclipse.emf.cdo.server.ISessionManager;
import org.eclipse.emf.cdo.server.SessionCreationException;

import org.eclipse.net4j.util.security.IUserManager;

import java.util.List;

/**
 * @author Eike Stepper
 */
public interface InternalSessionManager extends ISessionManager
{
  public InternalRepository getRepository();

  public void setRepository(InternalRepository repository);

  public void setUserManager(IUserManager userManager);

  public InternalSession[] getSessions();

  /**
   * @since 2.0
   */
  public InternalSession getSession(int sessionID);

  /**
   * @return Never <code>null</code>
   * @since 2.0
   */
  public InternalSession openSession(ISessionProtocol sessionProtocol) throws SessionCreationException;

  public void handleCommitNotification(long timeStamp, CDOPackageUnit[] packageUnits, List<CDOIDAndVersion> dirtyIDs,
      List<CDOID> detachedObjects, List<CDORevisionDelta> deltas, InternalSession excludedSession);
}
