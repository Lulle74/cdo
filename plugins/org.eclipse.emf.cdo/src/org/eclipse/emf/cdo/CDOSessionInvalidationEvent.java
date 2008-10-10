/***************************************************************************
 * Copyright (c) 2004 - 2008 Eike Stepper, Germany.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    Eike Stepper - initial API and implementation
 *    Simon McDuff - maintenance
 **************************************************************************/
package org.eclipse.emf.cdo;

import org.eclipse.emf.cdo.common.id.CDOID;
import org.eclipse.emf.cdo.common.id.CDOIDAndVersion;

import java.util.Collection;
import java.util.Set;

/**
 * @author Eike Stepper
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface CDOSessionInvalidationEvent extends CDOSessionEvent
{
  public static final long LOCAL_ROLLBACK = 0L;

  /**
   * Returns the transaction that was committed and thereby caused this event to be emitted if this transaction is
   * local, or <code>null</code> if the transaction was remote.
   */
  public CDOView getView();

  /**
   * Returns the time stamp of the server transaction if this event was sent as a result of a successfully committed
   * transaction or <code>LOCAL_ROLLBACK</code> if this event was sent due to a local rollback.
   */
  public long getTimeStamp();

  public Set<CDOIDAndVersion> getDirtyOIDs();

  /**
   * @since 2.0
   */
  public Collection<CDOID> getDetachedObjects();
}
