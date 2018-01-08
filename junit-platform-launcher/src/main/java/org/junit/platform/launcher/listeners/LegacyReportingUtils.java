/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher.listeners;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import org.apiguardian.api.API;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

@API(status = EXPERIMENTAL, since = "1.1")
public class LegacyReportingUtils {

	public static String getLegacyReportingClassName(TestPlan testPlan, TestIdentifier testIdentifier) {
		for (TestIdentifier current = testIdentifier; current != null; current = getParent(testPlan, current)) {
			ClassSource source = getClassSource(current);
			if (source != null) {
				return source.getClassName();
			}
		}
		return getParentLegacyReportingName(testPlan, testIdentifier);
	}

	private static TestIdentifier getParent(TestPlan testPlan, TestIdentifier testIdentifier) {
		return testPlan.getParent(testIdentifier).orElse(null);
	}

	private static ClassSource getClassSource(TestIdentifier current) {
		// @formatter:off
		return current.getSource()
				.filter(ClassSource.class::isInstance)
				.map(ClassSource.class::cast)
				.orElse(null);
		// @formatter:on
	}

	private static String getParentLegacyReportingName(TestPlan testPlan, TestIdentifier testIdentifier) {
		// @formatter:off
		return testPlan.getParent(testIdentifier)
				.map(TestIdentifier::getLegacyReportingName)
				.orElse("<unrooted>");
		// @formatter:on
	}
}
