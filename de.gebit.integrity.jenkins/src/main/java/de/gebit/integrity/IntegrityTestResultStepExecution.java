/*******************************************************************************
 * Copyright (c) 2017 Rene Schneider, GEBIT Solutions GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package de.gebit.integrity;

import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousNonBlockingStepExecution;

/**
 *
 *
 * @author Rene Schneider - initial API and implementation
 *
 */
public class IntegrityTestResultStepExecution extends AbstractSynchronousNonBlockingStepExecution<Void> {

	@Override
	protected Void run() throws Exception {
		System.out.println("Running Integrity step");
		return null;
	}
}
