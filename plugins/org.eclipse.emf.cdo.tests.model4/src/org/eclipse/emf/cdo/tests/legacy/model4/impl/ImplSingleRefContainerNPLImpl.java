/**
 * Copyright (c) 2004 - 2009 Eike Stepper (Berlin, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    Eike Stepper - initial API and implementation
 *
 * $Id: ImplSingleRefContainerNPLImpl.java,v 1.4 2009-08-12 17:15:31 estepper Exp $
 */
package org.eclipse.emf.cdo.tests.legacy.model4.impl;

import org.eclipse.emf.cdo.tests.legacy.model4.ImplSingleRefContainerNPL;
import org.eclipse.emf.cdo.tests.legacy.model4.model4Package;
import org.eclipse.emf.cdo.tests.legacy.model4interfaces.IContainedElementNoParentLink;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Impl Single Ref Container NPL</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.eclipse.emf.cdo.tests.legacy.model4.impl.ImplSingleRefContainerNPLImpl#getElement <em>Element</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class ImplSingleRefContainerNPLImpl extends EObjectImpl implements ImplSingleRefContainerNPL
{
  /**
   * The cached value of the '{@link #getElement() <em>Element</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getElement()
   * @generated
   * @ordered
   */
  protected IContainedElementNoParentLink element;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected ImplSingleRefContainerNPLImpl()
  {
    super();
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  protected EClass eStaticClass()
  {
    return model4Package.Literals.IMPL_SINGLE_REF_CONTAINER_NPL;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public IContainedElementNoParentLink getElement()
  {
    return element;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public NotificationChain basicSetElement(IContainedElementNoParentLink newElement, NotificationChain msgs)
  {
    IContainedElementNoParentLink oldElement = element;
    element = newElement;
    if (eNotificationRequired())
    {
      ENotificationImpl notification = new ENotificationImpl(this, Notification.SET,
          model4Package.IMPL_SINGLE_REF_CONTAINER_NPL__ELEMENT, oldElement, newElement);
      if (msgs == null)
        msgs = notification;
      else
        msgs.add(notification);
    }
    return msgs;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setElement(IContainedElementNoParentLink newElement)
  {
    if (newElement != element)
    {
      NotificationChain msgs = null;
      if (element != null)
        msgs = ((InternalEObject)element).eInverseRemove(this, EOPPOSITE_FEATURE_BASE
            - model4Package.IMPL_SINGLE_REF_CONTAINER_NPL__ELEMENT, null, msgs);
      if (newElement != null)
        msgs = ((InternalEObject)newElement).eInverseAdd(this, EOPPOSITE_FEATURE_BASE
            - model4Package.IMPL_SINGLE_REF_CONTAINER_NPL__ELEMENT, null, msgs);
      msgs = basicSetElement(newElement, msgs);
      if (msgs != null)
        msgs.dispatch();
    }
    else if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, model4Package.IMPL_SINGLE_REF_CONTAINER_NPL__ELEMENT,
          newElement, newElement));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs)
  {
    switch (featureID)
    {
    case model4Package.IMPL_SINGLE_REF_CONTAINER_NPL__ELEMENT:
      return basicSetElement(null, msgs);
    }
    return super.eInverseRemove(otherEnd, featureID, msgs);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public Object eGet(int featureID, boolean resolve, boolean coreType)
  {
    switch (featureID)
    {
    case model4Package.IMPL_SINGLE_REF_CONTAINER_NPL__ELEMENT:
      return getElement();
    }
    return super.eGet(featureID, resolve, coreType);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public void eSet(int featureID, Object newValue)
  {
    switch (featureID)
    {
    case model4Package.IMPL_SINGLE_REF_CONTAINER_NPL__ELEMENT:
      setElement((IContainedElementNoParentLink)newValue);
      return;
    }
    super.eSet(featureID, newValue);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public void eUnset(int featureID)
  {
    switch (featureID)
    {
    case model4Package.IMPL_SINGLE_REF_CONTAINER_NPL__ELEMENT:
      setElement((IContainedElementNoParentLink)null);
      return;
    }
    super.eUnset(featureID);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public boolean eIsSet(int featureID)
  {
    switch (featureID)
    {
    case model4Package.IMPL_SINGLE_REF_CONTAINER_NPL__ELEMENT:
      return element != null;
    }
    return super.eIsSet(featureID);
  }

} //ImplSingleRefContainerNPLImpl
