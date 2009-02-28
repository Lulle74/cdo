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
package org.eclipse.emf.cdo.spi.common.model;

import org.eclipse.emf.cdo.common.id.CDOIDMetaRange;
import org.eclipse.emf.cdo.common.io.CDODataInput;
import org.eclipse.emf.cdo.common.io.CDODataOutput;
import org.eclipse.emf.cdo.common.model.CDOPackageInfo;

import org.eclipse.emf.ecore.EPackage;

import java.io.IOException;

/**
 * @author Eike Stepper
 */
public interface InternalCDOPackageInfo extends CDOPackageInfo
{
  public InternalCDOPackageUnit getPackageUnit();

  public void setPackageUnit(InternalCDOPackageUnit packageUnit);

  public void setPackageURI(String packageUri);

  public void setParentURI(String parentUri);

  public void setMetaIDRange(CDOIDMetaRange metaIdRange);

  public void setEPackage(EPackage ePackage);

  public void write(CDODataOutput out) throws IOException;

  public void read(CDODataInput in) throws IOException;
}
