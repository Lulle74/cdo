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
package org.eclipse.emf.cdo.tests.db.capabilities;

import org.eclipse.net4j.db.DBUtil;
import org.eclipse.net4j.db.IDBConnectionProvider;
import org.eclipse.net4j.util.io.TMPUtil;

import org.apache.derby.jdbc.EmbeddedDataSource;

import java.io.File;

/**
 * @author Stefan Winkler
 */
public class DerbyTest extends AbstractCapabilityTest
{
  public DerbyTest()
  {
    super("derby");
    File dbFolder = TMPUtil.createTempFolder("derby_", "_test", new File("c:/temp"));
    dbFolder.delete();
    EmbeddedDataSource derbyds = new EmbeddedDataSource();
    derbyds.setDatabaseName(dbFolder.getAbsolutePath());
    derbyds.setCreateDatabase("create");
    provider = DBUtil.createConnectionProvider(derbyds);

  }

  IDBConnectionProvider provider = null;

  @Override
  protected IDBConnectionProvider getConnectionProvider()
  {
    return provider;
  }
}
