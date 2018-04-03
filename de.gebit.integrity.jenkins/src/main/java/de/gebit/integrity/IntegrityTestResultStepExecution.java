/*******************************************************************************
 * Copyright (c) 2017 Rene Schneider, GEBIT Solutions GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package de.gebit.integrity;

import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousNonBlockingStepExecution;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;

/**
 *
 *
 * @author Rene Schneider - initial API and implementation
 *
 */
public class IntegrityTestResultStepExecution extends AbstractSynchronousNonBlockingStepExecution<Void> {

	/**
	 * Serial Version.
	 */
	private static final long serialVersionUID = -4922483821172053846L;

	@StepContextParameter
	private transient TaskListener listener;

	@StepContextParameter
	private transient FilePath workspace;

	@StepContextParameter
	private transient Run build;

	@StepContextParameter
	private transient Launcher launcher;

	@Override
	protected Void run() throws Exception {
		IntegrityTestResultRecorder tempRecorder = new IntegrityTestResultRecorder("*.html", false, false);

		tempRecorder.perform(build, workspace, launcher, listener);

		return null;
	}
}
