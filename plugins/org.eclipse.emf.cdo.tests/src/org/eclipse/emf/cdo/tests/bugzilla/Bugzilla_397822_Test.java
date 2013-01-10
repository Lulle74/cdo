/*
 * Copyright (c) 2004 - 2012 Eike Stepper (Berlin, Germany) and others.
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
import org.eclipse.emf.cdo.common.revision.delta.CDOFeatureDelta;
import org.eclipse.emf.cdo.common.revision.delta.CDOListFeatureDelta;
import org.eclipse.emf.cdo.common.revision.delta.CDORevisionDelta;
import org.eclipse.emf.cdo.eresource.CDOResource;
import org.eclipse.emf.cdo.internal.common.revision.delta.CDORemoveFeatureDeltaImpl;
import org.eclipse.emf.cdo.session.CDOSession;
import org.eclipse.emf.cdo.tests.AbstractCDOTest;
import org.eclipse.emf.cdo.tests.config.IModelConfig;
import org.eclipse.emf.cdo.tests.model6.UnorderedList;
import org.eclipse.emf.cdo.transaction.CDOTransaction;
import org.eclipse.emf.cdo.util.CDOUtil;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EReference;

import java.util.Arrays;
import java.util.List;

/**
 * Bug 397822: [Legacy] REMOVE_MANY events are not transferred correctly to CDORevision
 *
 * @author Eike Stepper
 */
public class Bugzilla_397822_Test extends AbstractCDOTest
{
  @Skips(IModelConfig.CAPABILITY_LEGACY)
  public void testRemoveAll() throws Exception
  {
    CDOSession session = openSession();
    CDOTransaction transaction = session.openTransaction();
    CDOResource resource = transaction.createResource(getResourcePath("/resource1"));

    UnorderedList elem1 = getModel6Factory().createUnorderedList();
    UnorderedList elem2 = getModel6Factory().createUnorderedList();
    UnorderedList elem3 = getModel6Factory().createUnorderedList();
    UnorderedList elem4 = getModel6Factory().createUnorderedList();
    UnorderedList elem5 = getModel6Factory().createUnorderedList();

    UnorderedList object = getModel6Factory().createUnorderedList();
    CDOObject cdoObject = CDOUtil.getCDOObject(object);

    EList<UnorderedList> list = object.getContained();
    list.addAll(Arrays.asList(elem1, elem2, elem3, elem4, elem5));

    resource.getContents().add(object);
    transaction.commit();

    list.removeAll(Arrays.asList(elem2, elem4));

    CDOID id = cdoObject.cdoID();
    CDORevisionDelta revisionDelta = transaction.getRevisionDeltas().get(id);
    EReference reference = getModel6Package().getUnorderedList_Contained();

    assertRevisionDeltaContainsListChanges(revisionDelta //
        // removal of elem4 at index 3
        , new CDORemoveFeatureDeltaImpl(reference, 3)

        // removal of elem2 at index 1
        , new CDORemoveFeatureDeltaImpl(reference, 1)

    // TODO Clarify where the following delta is supposed to come from (see bug 390283)
    // // elem5 (at index 3 after the two removals) takes elem2's place at index 1
    // , new CDOMoveFeatureDeltaImpl(reference, 1, 2)

    );
  }

  private void assertRevisionDeltaContainsListChanges(CDORevisionDelta revisionDelta,
      CDOFeatureDelta... expectedListChanges)
  {
    EReference unorderedList_Contained = getModel6Package().getUnorderedList_Contained();
    CDOFeatureDelta featureDelta = revisionDelta.getFeatureDelta(unorderedList_Contained);

    assertInstanceOf(CDOListFeatureDelta.class, featureDelta);

    List<CDOFeatureDelta> listChanges = ((CDOListFeatureDelta)featureDelta).getListChanges();
    CDOFeatureDelta[] actualListChanges = listChanges.toArray(new CDOFeatureDelta[listChanges.size()]);
    assertEquals(Arrays.deepToString(expectedListChanges), Arrays.deepToString(actualListChanges));
  }
}
