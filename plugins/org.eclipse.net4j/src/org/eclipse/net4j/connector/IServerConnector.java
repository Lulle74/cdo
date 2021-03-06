/*
 * Copyright (c) 2016, 2021 Eike Stepper (Loehne, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eike Stepper - initial API and implementation
 */
package org.eclipse.net4j.connector;

import org.eclipse.net4j.acceptor.IAcceptor;

/**
 * @author Eike Stepper
 * @since 4.5
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IServerConnector extends IConnector
{
  public IAcceptor getAcceptor();
}
