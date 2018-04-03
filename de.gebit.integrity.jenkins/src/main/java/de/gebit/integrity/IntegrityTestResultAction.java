/*******************************************************************************
 * Copyright (c) 2013 Rene Schneider, GEBIT Solutions GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package de.gebit.integrity;

import org.jvnet.localizer.Localizable;
import org.kohsuke.stapler.StaplerProxy;
import org.kohsuke.stapler.export.Exported;

import com.thoughtworks.xstream.XStream;

import hudson.model.BuildListener;
import hudson.model.HealthReport;
import hudson.tasks.test.AbstractTestResultAction;
import hudson.util.HeapSpaceStringConverter;
import hudson.util.XStream2;

/**
 * This result action is responsible for displaying the test result overview on the page of individual builds.
 * 
 * @author Rene Schneider - initial API and implementation
 */
public class IntegrityTestResultAction extends AbstractTestResultAction<IntegrityTestResultAction>
		implements StaplerProxy {

	/**
	 * The result to display.
	 */
	private IntegrityCompoundTestResult result;

	/**
	 * The XStream instance used for result persistence.
	 */
	private static final XStream XSTREAM = new XStream2();

	static {
		XSTREAM.alias("result", IntegrityCompoundTestResult.class);
		XSTREAM.registerConverter(new HeapSpaceStringConverter(), 100);
	}

	/**
	 * The action URL part.
	 */
	public static final String ACTION_URL = "integrityReport";

	/**
	 * Creates a new instance.
	 * 
	 * @param aResult
	 *            the compound test result to display
	 * @param aListener
	 *            the listener
	 */
	public IntegrityTestResultAction(IntegrityCompoundTestResult aResult, BuildListener aListener) {
		result = aResult;
		aResult.setParentAction(this);
	}

	@Override
	public Object readResolve() {
		Object tempObject = super.readResolve();
		if (result != null) {
			result.setParentAction(this);
		}
		return tempObject;
	}

	@Override
	public synchronized IntegrityCompoundTestResult getResult() {
		return result;
	}

	@Override
	public String getDisplayName() {
		return de.gebit.integrity.Messages.testResultActionDisplayName();
	}

	@Override
	@Exported(visibility = 2)
	public String getUrlName() {
		return ACTION_URL;
	}

	@Override
	public Object getTarget() {
		return result;
	}

	@Override
	public String getIconFileName() {
		if (getFailCount() == 0) {
			if (getExceptionCount() > 0) {
				return "/plugin/de.gebit.integrity.jenkins/integrity_icon_exception.png";
			} else {
				return "/plugin/de.gebit.integrity.jenkins/integrity_icon_success.png";
			}
		} else {
			return "/plugin/de.gebit.integrity.jenkins/integrity_icon_failure.png";
		}
	}

	/**
	 * Returns the health report of this build, based on the result.
	 */
	@Override
	public HealthReport getBuildHealth() {
		final int tempTotalCount = getTotalCount();
		final int tempFailCount = getSkipCount() + getFailCount();
		int tempScore = (tempTotalCount == 0) ? 100 : (int) (100.0 * (1.0 - ((double) tempFailCount) / tempTotalCount));
		Localizable tempDescription = null;
		if (tempTotalCount == 0) {
			tempDescription = de.gebit.integrity.Messages._noTestResult();
		} else {
			tempDescription = de.gebit.integrity.Messages._testResult(getPassCount(), getFailCount(), getSkipCount(),
					getCallExceptionCount());
		}
		return new HealthReport(tempScore, tempDescription);
	}

	public String getSummary() {
		return de.gebit.integrity.Messages.testResult(getPassCount(), getFailCount(), getSkipCount(),
				getCallExceptionCount());
	}

	@Override
	@Exported(visibility = 2)
	public int getFailCount() {
		return getResult().getFailCount();
	}

	@Override
	@Exported(visibility = 2)
	public int getTotalCount() {
		return getResult().getTotalCount();
	}

	@Override
	@Exported(visibility = 2)
	public int getSkipCount() {
		return getResult().getSkipCount();
	}

	@Exported(visibility = 2)
	public int getPassCount() {
		return getResult().getPassCount();
	}

	@Exported(visibility = 2)
	public int getExceptionCount() {
		return getTestExceptionCount() + getCallExceptionCount();
	}

	@Exported(visibility = 2)
	public int getTestExceptionCount() {
		return getResult().getTestExceptionCount();
	}

	@Exported(visibility = 2)
	public int getCallExceptionCount() {
		return getResult().getCallExceptionCount();
	}
}
