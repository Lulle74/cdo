/*
 * Copyright (c) 2015 Eike Stepper (Berlin, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eike Stepper - initial API and implementation
 */
package org.eclipse.emf.cdo.tests.bugzilla;

import org.eclipse.emf.cdo.CDOObject;
import org.eclipse.emf.cdo.common.id.CDOID;
import org.eclipse.emf.cdo.common.lock.CDOLockState;
import org.eclipse.emf.cdo.common.lock.IDurableLockingManager.LockGrade;
import org.eclipse.emf.cdo.server.StoreThreadLocal;
import org.eclipse.emf.cdo.session.CDOSession;
import org.eclipse.emf.cdo.spi.server.InternalSession;
import org.eclipse.emf.cdo.tests.AbstractLockingTest;
import org.eclipse.emf.cdo.tests.model1.Category;
import org.eclipse.emf.cdo.tests.model1.Company;
import org.eclipse.emf.cdo.util.CDOUtil;

import org.eclipse.net4j.util.concurrent.IRWLockManager.LockType;

import org.eclipse.emf.spi.cdo.InternalCDOTransaction;

import org.eclipse.core.runtime.NullProgressMonitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * Test ensuring that locks are correctly released on deleted objects.
 * @author Alex Lagarde
 */
public class Bugzilla_423699_Test extends AbstractLockingTest
{
  private CDOSession session;

  private InternalCDOTransaction tx;

  private CDOObject container;

  private CDOObject child;

  @Override
  protected void doSetUp() throws Exception
  {
    super.doSetUp();

    // Open a session and a transaction
    session = openSession();
    tx = (InternalCDOTransaction)session.openTransaction();

    // Create semantic model
    Company company = getModel1Factory().createCompany();
    Category category = getModel1Factory().createCategory();
    company.getCategories().add(category);
    tx.getOrCreateResource(getResourcePath("testResource")).getContents().add(company);
    tx.commit();

    container = CDOUtil.getCDOObject(company);
    child = CDOUtil.getCDOObject(category);
  }

  @Override
  protected void doTearDown() throws Exception
  {
    tx.close();
    tx = null;

    session.close();
    session = null;
    super.doTearDown();
  }

  public void testUnlockDeletedElementsWithDurableLockingAndAutoReleaseLocks() throws Exception
  {
    doTestUnlockDeletedElements(true, true);
  }

  public void testUnlockDeletedElementsWithDurableLockingAndNoAutoReleaseLocks() throws Exception
  {
    doTestUnlockDeletedElements(true, false);
  }

  public void testUnlockDeletedElementsWithNoDurableLockingAndAutoReleaseLocks() throws Exception
  {
    doTestUnlockDeletedElements(false, true);
  }

  public void testUnlockDeletedElementsWithNoDurableLockingAndNoAutoReleaseLocks() throws Exception
  {
    doTestUnlockDeletedElements(false, false);
  }

  /**
   * Ensures that locks are correctly released on deleted objects.
   * @param durableLocking indicates if durableLocking should be activated or not
   * @param autoReleaseLocksEnabled indicates if locks should be release on commit or not
   * @throws Exception if issue occurs while locking or committing elements
   */
  private void doTestUnlockDeletedElements(boolean durableLocking, boolean autoReleaseLocksEnabled) throws Exception
  {
    // Step 1: update transaction options
    tx.options().setAutoReleaseLocksEnabled(autoReleaseLocksEnabled);
    if (durableLocking)
    {
      tx.enableDurableLocking();
    }

    CDOID containerID = container.cdoID();
    CDOID childID = child.cdoID();

    // Step 2: lock the root and its child
    Collection<CDOObject> objectsToLock = new LinkedHashSet<CDOObject>();
    objectsToLock.add(container);
    objectsToLock.add(child);

    tx.lockObjects(objectsToLock, LockType.WRITE, 10000);
    assertIsLocked(durableLocking, true, containerID);
    assertIsLocked(durableLocking, true, childID);

    // Step 3: delete child from its container
    ((Company)CDOUtil.getEObject(container)).getCategories().clear();

    // Step 4: commit
    tx.commit(new NullProgressMonitor());

    // Lock should be deleted on detached object
    assertIsLocked(durableLocking, false, childID);

    // Lock should be deleted only if lock autorelease is enabled
    assertIsLocked(durableLocking, !autoReleaseLocksEnabled, containerID);

    // Step 5 (optional): reopen transaction & session with the same durable locking ID
    if (durableLocking)
    {
      String durableLockingID = tx.getDurableLockingID();
      tx.close();
      session.close();
      session = openSession();
      tx = (InternalCDOTransaction)session.openTransaction(durableLockingID);
      tx.options().setAutoReleaseLocksEnabled(autoReleaseLocksEnabled);

      // Lock states should not have changed
      assertIsLocked(durableLocking, false, childID);
      assertIsLocked(durableLocking, !autoReleaseLocksEnabled, containerID);
    }

    // Step 6: unlock all elements
    tx.unlockObjects();
    assertIsLocked(durableLocking, false, containerID);
    assertIsLocked(durableLocking, false, childID);
  }

  /**
   * Ensures that the given element is locked or not (according to the given parameter), durably or not (according to the given parameter).
   * @param durably indicates if we expect a durable lock or not
   * @param shouldBeLocked indicates if elements should be locked or not
   * @param elementID {@link CDOID} of the element ot test
   */
  private void assertIsLocked(boolean durably, boolean shouldBeLocked, CDOID elementID)
  {
    // Step 1: check durable lock
    if (durably)
    {
      LockGrade durableLock = null;

      try
      {
        InternalSession session = getRepository().getSessionManager().getSession(tx.getSessionID());
        StoreThreadLocal.setSession(session);
        // Do your call
        durableLock = getRepository().getLockingManager().getLockArea(tx.getDurableLockingID()).getLocks()
            .get(elementID);
      }
      finally
      {
        StoreThreadLocal.release();
      }

      assertEquals(elementID + " has not the expected durable lock status", shouldBeLocked, durableLock != null);
    }

    // Step 2: check lock
    ArrayList<CDOID> elementIDs = new ArrayList<CDOID>();
    elementIDs.add(elementID);

    CDOLockState cdoLockState = tx.getLockStates(elementIDs)[0];
    assertEquals(elementID + " has wrong lock status", shouldBeLocked, cdoLockState.getWriteLockOwner() != null);
  }
}
