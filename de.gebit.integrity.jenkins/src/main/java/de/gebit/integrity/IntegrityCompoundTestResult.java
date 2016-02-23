/*******************************************************************************
 * Copyright (c) 2013 Rene Schneider, GEBIT Solutions GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package de.gebit.integrity;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import com.thoughtworks.xstream.XStream;

import hudson.XmlFile;
import hudson.model.AbstractBuild;
import hudson.tasks.test.AbstractTestResultAction;
import hudson.tasks.test.TabulatedResult;
import hudson.tasks.test.TestObject;
import hudson.tasks.test.TestResult;
import hudson.util.HeapSpaceStringConverter;
import hudson.util.XStream2;

/**
 * This test result combines multiple single test results into one. The single test results are children of this
 * compound.
 * 
 * @author Rene Schneider - initial API and implementation
 * 
 */
public class IntegrityCompoundTestResult extends TabulatedResult {

	/**
	 * The serialization version.
	 */
	private static final long serialVersionUID = 5660708469068256878L;

	/**
	 * The XStream instance used for result persistence.
	 */
	private static final XStream XSTREAM = new XStream2();

	static {
		XSTREAM.alias("result", IntegrityCompoundTestResult.class);
		XSTREAM.registerConverter(new HeapSpaceStringConverter(), 100);
	}

	/**
	 * The inner test results.
	 */
	private transient List<IntegrityTestResult> tempChildren;

	/**
	 * Whether we already updated child links.
	 */
	private transient boolean hasUpdatedChildLinks;

	/**
	 * Whether the currently loaded children are persisted.
	 */
	private transient boolean hasPersistedChildren = true;

	/**
	 * Total count of failed tests over all children.
	 */
	private int failCount;

	/**
	 * Total count of passed tests over all children.
	 */
	private int passCount;

	/**
	 * Total count of skipped tests over all children.
	 */
	private int skipCount;

	/**
	 * Total count of exceptions in tests over all children.
	 */
	private int testExceptionCount;

	/**
	 * Total count of exceptions in calls over all children.
	 */
	private int callExceptionCount;

	/**
	 * The action owning this result.
	 */
	private transient AbstractTestResultAction<?> parentAction;

	/**
	 * Adds a child (single test result).
	 * 
	 * @param aChild
	 *            the child to add
	 */
	public synchronized void addChild(IntegrityTestResult aChild) {
		if (tempChildren == null) {
			tempChildren = new ArrayList<IntegrityTestResult>();
		}

		tempChildren.add(aChild);
		aChild.setParent(this);
		hasPersistedChildren = false;

		sortChildren();
	}

	private XmlFile getXmlFile() {
		return new XmlFile(XSTREAM, new File(getOwner().getRootDir(), "integrityResultData.xml"));
	}

	private void sortChildren() {
		Collections.sort(tempChildren, new Comparator<IntegrityTestResult>() {

			public int compare(IntegrityTestResult o1, IntegrityTestResult o2) {
				String tempFirstName = o1.getDisplayName() != null ? o1.getDisplayName() : "";
				String tempSecondName = o2.getDisplayName() != null ? o2.getDisplayName() : "";

				return tempFirstName.compareToIgnoreCase(tempSecondName);
			}
		});
	}

	@SuppressWarnings("unchecked")
	private void loadChildren() {
		try {
			tempChildren = (List<IntegrityTestResult>) getXmlFile().read();
		} catch (IOException exc) {
			exc.printStackTrace();
			tempChildren = new ArrayList<IntegrityTestResult>();
		}

		hasPersistedChildren = true;
		sortChildren();
	}

	private void persistChildren() {
		if (tempChildren != null) {
			try {
				getXmlFile().write(tempChildren);
			} catch (IOException exc) {
				exc.printStackTrace();
			}
		}

		hasPersistedChildren = true;
	}

	private void updateCounts() {
		passCount = 0;
		failCount = 0;
		skipCount = 0;
		testExceptionCount = 0;
		callExceptionCount = 0;

		if (hasChildren()) {
			for (TestResult tempResult : getChildren()) {
				passCount += tempResult.getPassCount();
				failCount += tempResult.getFailCount();
				skipCount += tempResult.getSkipCount();
				if (tempResult instanceof IntegrityTestResult) {
					testExceptionCount += ((IntegrityTestResult) tempResult).getTestExceptionCount();
					callExceptionCount += ((IntegrityTestResult) tempResult).getCallExceptionCount();
				}
			}
		}
	}

	private void writeObject(ObjectOutputStream stream) throws IOException {
		updateCounts();

		stream.defaultWriteObject();

		if (!hasPersistedChildren) {
			persistChildren();
		}
	}

	public String getDisplayName() {
		return "Integrity Test Results";
	}

	@Override
	public TestObject getParent() {
		return null;
	}

	@Override
	public AbstractBuild<?, ?> getOwner() {
		return (parentAction == null ? null : parentAction.owner);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void setParentAction(AbstractTestResultAction aParentAction) {
		parentAction = aParentAction;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public AbstractTestResultAction getParentAction() {
		return parentAction;
	}

	@Override
	public Object getDynamic(String aToken, StaplerRequest aRequest, StaplerResponse aResponse) {
		TestResult tempResult = findCorrespondingResult(aToken);
		if (tempResult != null) {
			return tempResult;
		}
		return null;
	}

	@Override
	public TestResult findCorrespondingResult(String anId) {
		if (getId().equals(anId) || (anId == null)) {
			return this;
		} else {
			if (hasChildren()) {
				// For some totally unexplainable reason (no, really...I spent several hours to find an explanation, but
				// was unsuccessful) the Jenkins sometimes designates an ID of "(empty)" to instances of this class. If
				// it does, the children automatically expand their ID, so the queried ID does not match anymore. I
				// simply expand it as well here :-)
				String tempChildId = (getId() != null && getId().length() > 0) ? getId() + "/" + anId : anId;

				for (TestResult tempChild : getChildren()) {
					TestResult tempResult = tempChild.findCorrespondingResult(tempChildId);
					if (tempResult != null) {
						return tempResult;
					}
				}
			}
			return null;
		}
	}

	@Override
	public boolean hasChildren() {
		return getChildren().size() > 0;
	}

	@Override
	public Collection<? extends TestResult> getChildren() {
		if (tempChildren == null) {
			loadChildren();
		}

		if (!hasUpdatedChildLinks) {
			for (IntegrityTestResult tempChild : tempChildren) {
				tempChild.setParent(this);
			}
			hasUpdatedChildLinks = true;
		}

		return tempChildren;
	}

	@Override
	public int getFailCount() {
		return failCount;
	}

	@Override
	public int getPassCount() {
		return passCount;
	}

	@Override
	public int getSkipCount() {
		return skipCount;
	}

	public int getTestExceptionCount() {
		return testExceptionCount;
	}

	public int getCallExceptionCount() {
		return callExceptionCount;
	}

	/**
	 * Returns the total exception count.
	 * 
	 * @return
	 */
	public int getExceptionCount() {
		return getTestExceptionCount() + getCallExceptionCount();
	}

	/**
	 * Gets the counter part of this {@link TestResult} in the specified run. This basically equals the upstream
	 * function that is overridden here, but it also sets the parent action.
	 * 
	 * @return null if no such counter part exists.
	 */
	@Override
	public TestResult getResultInBuild(AbstractBuild<?, ?> aBuild) {
		AbstractTestResultAction<?> tempTestResultAction = aBuild.getAction(getParentAction().getClass());
		if (tempTestResultAction == null) {
			tempTestResultAction = aBuild.getAction(AbstractTestResultAction.class);
		}
		if (tempTestResultAction == null) {
			return null;
		} else {
			TestResult tempResult = tempTestResultAction.findCorrespondingResult(this.getId());
			if (tempResult != null) {
				tempResult.setParentAction(tempTestResultAction);
			}
			return tempResult;
		}
	}

}
