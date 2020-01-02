/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.reporting.legacy;

import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;
import org.junit.platform.launcher.TestPlan;

/**
 * @since 1.0.3
 */
class LegacyReportingUtilsTests {

	private TestDescriptor engineDescriptor = new EngineDescriptor(UniqueId.forEngine("foo"), "Foo");

	@Test
	void legacyReportingClassNameForTestIdentifierWithoutClassSourceIsParentLegacyReportingName() {
		UniqueId uniqueId = engineDescriptor.getUniqueId().append("child", "bar");
		TestDescriptor testDescriptor = createTestDescriptor(uniqueId, "Bar", null);
		engineDescriptor.addChild(testDescriptor);

		assertThat(getClassName(engineDescriptor.getUniqueId())).isEqualTo("<unrooted>");
		assertThat(getClassName(uniqueId)).isEqualTo("Foo");

		assertThat(getClassNameFromOldLocation(engineDescriptor.getUniqueId())).isEqualTo("<unrooted>");
		assertThat(getClassNameFromOldLocation(uniqueId)).isEqualTo("Foo");
	}

	@Test
	void legacyReportingClassNameForDescendantOfTestIdentifierWithClassSourceIsClassName() {
		UniqueId classUniqueId = engineDescriptor.getUniqueId().append("class", "class");
		TestDescriptor classDescriptor = createTestDescriptor(classUniqueId, "Class",
			ClassSource.from(LegacyReportingUtilsTests.class));
		engineDescriptor.addChild(classDescriptor);

		UniqueId subUniqueId = classUniqueId.append("sub", "baz");
		TestDescriptor subDescriptor = createTestDescriptor(subUniqueId, "Baz", null);
		classDescriptor.addChild(subDescriptor);

		UniqueId subSubUniqueId = subUniqueId.append("subsub", "qux");
		TestDescriptor subSubDescriptor = createTestDescriptor(subSubUniqueId, "Qux", null);
		subDescriptor.addChild(subSubDescriptor);

		assertThat(getClassName(engineDescriptor.getUniqueId())).isEqualTo("<unrooted>");
		assertThat(getClassName(classUniqueId)).isEqualTo(LegacyReportingUtilsTests.class.getName());
		assertThat(getClassName(subUniqueId)).isEqualTo(LegacyReportingUtilsTests.class.getName());
		assertThat(getClassName(subSubUniqueId)).isEqualTo(LegacyReportingUtilsTests.class.getName());

		assertThat(getClassNameFromOldLocation(engineDescriptor.getUniqueId())).isEqualTo("<unrooted>");
		assertThat(getClassNameFromOldLocation(classUniqueId)).isEqualTo(LegacyReportingUtilsTests.class.getName());
		assertThat(getClassNameFromOldLocation(subUniqueId)).isEqualTo(LegacyReportingUtilsTests.class.getName());
		assertThat(getClassNameFromOldLocation(subSubUniqueId)).isEqualTo(LegacyReportingUtilsTests.class.getName());
	}

	private String getClassName(UniqueId uniqueId) {
		TestPlan testPlan = TestPlan.from(singleton(engineDescriptor));
		return LegacyReportingUtils.getClassName(testPlan, testPlan.getTestIdentifier(uniqueId.toString()));
	}

	@SuppressWarnings("deprecation")
	private String getClassNameFromOldLocation(UniqueId uniqueId) {
		TestPlan testPlan = TestPlan.from(singleton(engineDescriptor));
		return org.junit.platform.launcher.listeners.LegacyReportingUtils.getClassName(testPlan,
			testPlan.getTestIdentifier(uniqueId.toString()));
	}

	private TestDescriptor createTestDescriptor(UniqueId uniqueId, String displayName, TestSource source) {
		return new AbstractTestDescriptor(uniqueId, displayName, source) {
			@Override
			public Type getType() {
				return Type.CONTAINER_AND_TEST;
			}
		};
	}
}
