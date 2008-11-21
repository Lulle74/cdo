/***************************************************************************
 * Copyright (c) 2004 - 2008 Eike Stepper, Germany.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    Simon McDuff - initial API and implementation
 **************************************************************************/
package org.eclipse.emf.cdo.tests.bugzilla;

import org.eclipse.emf.cdo.CDOSession;
import org.eclipse.emf.cdo.CDOTransaction;
import org.eclipse.emf.cdo.eresource.CDOResource;
import org.eclipse.emf.cdo.tests.AbstractCDOTest;
import org.eclipse.emf.cdo.tests.model3.subpackage.Class2;

/**
 * Lazy packageRegistry fail when adding instance with Eclass in a subpackage See https://bugs.eclipse.org/256141
 * 
 * @author Simon McDuff
 */
public class Bugzilla_256141_Test extends AbstractCDOTest
{
  public void testBugzilla_256141() throws InterruptedException
  {
    CDOSession session = openLazySession();
    CDOTransaction transaction = session.openTransaction();

    CDOResource resource1 = transaction.createResource("test1");

    Class2 class2 = getModel3SubpackageFactory().createClass2();
    resource1.getContents().add(class2);

    transaction.commit();

  }
}
