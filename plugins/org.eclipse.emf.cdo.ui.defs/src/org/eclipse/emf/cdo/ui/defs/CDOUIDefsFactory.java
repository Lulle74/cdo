/**
 * Copyright (c) 2004 - 2009 Eike Stepper (Berlin, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    Andre Dietisheim - initial API and implementation
 *    Eike Stepper - maintenance
 */
package org.eclipse.emf.cdo.ui.defs;

import org.eclipse.emf.ecore.EFactory;

/**
 * <!-- begin-user-doc --> The <b>Factory</b> for the model. It provides a create method for each non-abstract class of
 * the model. <!-- end-user-doc -->
 * @see org.eclipse.emf.cdo.ui.defs.CDOUIDefsPackage
 * @generated
 */
public interface CDOUIDefsFactory extends EFactory
{
  /**
   * The singleton instance of the factory.
   * <!-- begin-user-doc --> <!-- end-user-doc -->
   * @generated
   */
  CDOUIDefsFactory eINSTANCE = org.eclipse.emf.cdo.ui.defs.impl.CDOUIDefsFactoryImpl.init();

  /**
   * Returns a new object of class '<em>Editor Def</em>'.
   * <!-- begin-user-doc --> <!-- end-user-doc -->
   * @return a new object of class '<em>Editor Def</em>'.
   * @generated
   */
  EditorDef createEditorDef();

  /**
   * Returns a new object of class '<em>CDO Editor Def</em>'.
   * <!-- begin-user-doc --> <!-- end-user-doc -->
   * @return a new object of class '<em>CDO Editor Def</em>'.
   * @generated
   */
  CDOEditorDef createCDOEditorDef();

  /**
   * Returns the package supported by this factory.
   * <!-- begin-user-doc --> <!-- end-user-doc -->
   * @return the package supported by this factory.
   * @generated
   */
  CDOUIDefsPackage getCDOUIDefsPackage();

} // CDOUIDefsFactory
