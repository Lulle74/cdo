/***************************************************************************
 * Copyright (c) 2004 - 2008 Eike Stepper, Germany.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    Eike Stepper - initial API and implementation
 **************************************************************************/
package org.eclipse.emf.cdo.spi.common;

import org.eclipse.emf.cdo.common.CDODataOutput;
import org.eclipse.emf.cdo.common.model.CDOModelElement;
import org.eclipse.emf.cdo.common.model.CDOPackage;

import java.io.IOException;

/**
 * @author Eike Stepper
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface InternalCDOModelElement extends CDOModelElement
{
  /**
   * @since 2.0
   */
  public void setContainingPackage(CDOPackage containingPackage);

  public void setName(String name);

  public void setClientInfo(Object clientInfo);

  public void setServerInfo(Object serverInfo);

  /**
   * @since 2.0
   */
  public void write(CDODataOutput out) throws IOException;
}
