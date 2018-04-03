/*******************************************************************************
 * Copyright (c) 2017 Rene Schneider, GEBIT Solutions GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package de.gebit.integrity;

import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;

/**
 * The execution for pipeline builds.
 *
 * @author Rene Schneider - initial API and implementation
 *
 */
public class IntegrityTestResultStepExecution extends SynchronousNonBlockingStepExecution<Void> {

	/**
	 * Serial Version.
	 */
	private static final long serialVersionUID = -4922483821172053846L;

	/**
	 * The step.
	 */
	private final IntegrityTestResultStep step;

	public IntegrityTestResultStepExecution(IntegrityTestResultStep aStep, StepContext aContext) {
		super(aContext);
		step = aStep;
	}

	@Override
	protected Void run() throws Exception {
		Run<?, ?> tempBuild = getContext().get(Run.class);
		TaskListener tempListener = getContext().get(TaskListener.class);
		Launcher tempLauncher = getContext().get(Launcher.class);
		FilePath tempWorkspace = getContext().get(FilePath.class);

		IntegrityTestResultRecorder tempRecorder = new IntegrityTestResultRecorder(step.getTestResultFileNamePattern(),
				step.getIgnoreNoResults(), step.getFailOnTestErrors());

		tempRecorder.perform(tempBuild, tempWorkspace, tempLauncher, tempListener);

		return null;
	}
}
