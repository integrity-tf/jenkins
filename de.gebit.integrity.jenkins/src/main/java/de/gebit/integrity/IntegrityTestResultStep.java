/*******************************************************************************
 * Copyright (c) 2017 Rene Schneider, GEBIT Solutions GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package de.gebit.integrity;

import javax.annotation.Nonnull;

import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;

/**
 *
 *
 * @author Rene Schneider - initial API and implementation
 *
 */
public class IntegrityTestResultStep extends AbstractStepImpl {

	@DataBoundConstructor
	public IntegrityTestResultStep() {
	}

	@Extension
	public static class DescriptorImpl extends AbstractStepDescriptorImpl {
		public DescriptorImpl() {
			super(IntegrityTestResultStepExecution.class);
		}

		@Override
		public String getFunctionName() {
			return "Integrity Test Result Archive";
		}

		@Nonnull
		@Override
		public String getDisplayName() {
			return "Archive Integrity Test Results";
		}
	}

}
