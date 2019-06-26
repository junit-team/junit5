/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import java.util.List;

import org.junit.jupiter.engine.AbstractJupiterTestEngineTests;
import org.junit.platform.engine.TestDescriptor;

/**
 * Check Annotated requirements.
 *
 * @see Requirement
 */
class RequirementTests extends AbstractJupiterTestEngineTests {

	@Test
	void testRequirementAnnotation() {
		check(RequirementTestCase.class, List.of( //
			"RequirementTests$RequirementTestCase: null", //
			"testEmptyAnnotation(): null", //
			"testNoAnnotation(): null", "testRequirementWithId(): Req 11"));
	}

	private void check(Class<?> testClass, List<String> expectedRequirements) {
		var request = request().selectors(selectClass(testClass)).build();
		var descriptors = discoverTests(request).getDescendants();
		var sortedNamesRequirements = descriptors.stream().map(this::describe).sorted().collect(toList());
		assertLinesMatch(expectedRequirements, sortedNamesRequirements);
	}

	private String describe(TestDescriptor descriptor) {
		return descriptor.getDisplayName() + ": " + descriptor.getRequirement();
	}

	static class RequirementTestCase {
		@Requirement(id = "")
		@Test
		void testEmptyAnnotation() {
		}

		@Test
		void testNoAnnotation() {
		}

		@Requirement(id = "Req 11")
		@Test
		void testRequirementWithId() {
		}
	}

}
