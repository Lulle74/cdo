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
package org.eclipse.emf.cdo.security;

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Role</b></em>'.
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.emf.cdo.security.Role#getId <em>Id</em>}</li>
 *   <li>{@link org.eclipse.emf.cdo.security.Role#getPermissions <em>Permissions</em>}</li>
 *   <li>{@link org.eclipse.emf.cdo.security.Role#getAssignees <em>Assignees</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.emf.cdo.security.SecurityPackage#getRole()
 * @model
 * @generated
 */
public interface Role extends SecurityItem
{
  /**
   * Returns the value of the '<em><b>Assignees</b></em>' reference list.
   * The list contents are of type {@link org.eclipse.emf.cdo.security.Assignee}.
   * It is bidirectional and its opposite is '{@link org.eclipse.emf.cdo.security.Assignee#getRoles <em>Roles</em>}'.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Assignees</em>' reference list isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Assignees</em>' reference list.
   * @see org.eclipse.emf.cdo.security.SecurityPackage#getRole_Assignees()
   * @see org.eclipse.emf.cdo.security.Assignee#getRoles
   * @model opposite="roles"
   * @generated
   */
  EList<Assignee> getAssignees();

  /**
   * Returns the value of the '<em><b>Id</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Id</em>' attribute isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Id</em>' attribute.
   * @see #setId(String)
   * @see org.eclipse.emf.cdo.security.SecurityPackage#getRole_Id()
   * @model
   * @generated
   */
  String getId();

  /**
   * Sets the value of the '{@link org.eclipse.emf.cdo.security.Role#getId <em>Id</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Id</em>' attribute.
   * @see #getId()
   * @generated
   */
  void setId(String value);

  /**
   * Returns the value of the '<em><b>Permissions</b></em>' containment reference list.
   * The list contents are of type {@link org.eclipse.emf.cdo.security.Permission}.
   * It is bidirectional and its opposite is '{@link org.eclipse.emf.cdo.security.Permission#getRole <em>Role</em>}'.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Permissions</em>' containment reference list isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Permissions</em>' containment reference list.
   * @see org.eclipse.emf.cdo.security.SecurityPackage#getRole_Permissions()
   * @see org.eclipse.emf.cdo.security.Permission#getRole
   * @model opposite="role" containment="true"
   * @generated
   */
  EList<Permission> getPermissions();

} // Role