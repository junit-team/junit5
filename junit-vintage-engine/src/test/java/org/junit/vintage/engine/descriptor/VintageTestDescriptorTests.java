/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.vintage.engine.descriptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.runner.Description;

class VintageTestDescriptorTests {

	private static final UniqueId uniqueId = UniqueId.forEngine("vintage");

	@Test
	void constructFromInheritedMethod() {
		Description description = Description.createTestDescription(ConcreteTest.class, "theTest");
		VintageTestDescriptor descriptor = new VintageTestDescriptor(uniqueId, description);

		Optional<TestSource> sourceOptional = descriptor.getSource();
		assertThat(sourceOptional).containsInstanceOf(MethodSource.class);

		MethodSource methodSource = (MethodSource) sourceOptional.get();
		assertEquals(ConcreteTest.class.getName(), methodSource.getClassName());
		assertEquals("theTest", methodSource.getMethodName());
	}

	@Test
	void legacyReportingNameUsesClassName() {
		Description description = Description.createSuiteDescription(ConcreteTest.class);
		VintageTestDescriptor testDescriptor = new VintageTestDescriptor(uniqueId, description);

		assertEquals("org.junit.vintage.engine.descriptor.VintageTestDescriptorTests$ConcreteTest",
			testDescriptor.getLegacyReportingName());
	}

	@Test
	void legacyReportingNameUsesMethodName() {
		Description description = Description.createTestDescription(ConcreteTest.class, "legacyTest");
		VintageTestDescriptor testDescriptor = new VintageTestDescriptor(uniqueId, description);

		assertEquals("legacyTest", testDescriptor.getLegacyReportingName());
	}

	@Test
	void legacyReportingNameFallbackToDisplayName() {
		String suiteName = "Legacy Suite";
		Description description = Description.createSuiteDescription(suiteName);
		VintageTestDescriptor testDescriptor = new VintageTestDescriptor(uniqueId, description);

		assertEquals(testDescriptor.getDisplayName(), testDescriptor.getLegacyReportingName());
		assertEquals(suiteName, testDescriptor.getLegacyReportingName());
	}

	private abstract static class AbstractTestBase {

		@Test
		public void theTest() {
		}
	}

	private static class ConcreteTest extends AbstractTestBase {
	}
}
