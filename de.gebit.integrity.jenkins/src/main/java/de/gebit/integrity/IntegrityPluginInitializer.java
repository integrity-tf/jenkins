/*******************************************************************************
 * Copyright (c) 2022 Rene Schneider, GEBIT Solutions GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package de.gebit.integrity;

import hudson.init.InitMilestone;
import hudson.init.Initializer;
import hudson.model.Run;

/**
 *
 *
 * @author Rene Schneider - initial API and implementation
 *
 */
public class IntegrityPluginInitializer {

	@Initializer(before = InitMilestone.JOB_LOADED)
	public static void init() {
		// This is important to fix issue #25 (Testresult data serialized into build.xml and integrityResultData.xml)
		// Due to JEP-228 (https://github.com/jenkinsci/jep/blob/master/jep/228/README.adoc) the XStream annotations
		// used on IntegrityCompoundTestResult do not work automatically anymore and must be manually parsed.
		Run.XSTREAM.processAnnotations(IntegrityCompoundTestResult.class);
	}

}
