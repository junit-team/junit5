/*
 * Copyright 2015-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.extension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.engine.Constants.DEFAULT_TEST_CLASS_ORDER_PROPERTY_NAME;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.junit.platform.testkit.engine.Events;

/**
 * @since 5.8
 */
class OrderedClassTests {

	private static final Set<String> callSequence = Collections.synchronizedSet(new LinkedHashSet<>());

	@BeforeEach
	void clearCallSequence() {
		callSequence.clear();
	}

	@Test
	void className() {
		var tests = executeTests(ClassOrderer.ClassName.class);

		tests.assertStatistics(stats -> stats.succeeded(callSequence.size()));

		assertThat(callSequence)//
				.containsExactly("A_TestCase", "B_TestCase", "C_TestCase");
	}

	@Test
	void classNameAcrossPackages() {
		try {
			example.B_TestCase.callSequence = callSequence;

			// @formatter:off
			var tests = EngineTestKit
				.engine("junit-jupiter")
				.configurationParameter(DEFAULT_TEST_CLASS_ORDER_PROPERTY_NAME, ClassOrderer.ClassName.class.getName())
				.selectors(selectClass(B_TestCase.class), selectClass(example.B_TestCase.class))
				.execute()
				.testEvents();
			// @formatter:on

			tests.assertStatistics(stats -> stats.succeeded(callSequence.size()));

			assertThat(callSequence)//
					.containsExactly("example.B_TestCase", "B_TestCase");
		}
		finally {
			example.B_TestCase.callSequence = null;
		}
	}

	@Test
	void displayName() {
		var tests = executeTests(ClassOrderer.DisplayName.class);

		tests.assertStatistics(stats -> stats.succeeded(callSequence.size()));

		assertThat(callSequence)//
				.containsExactly("C_TestCase", "B_TestCase", "A_TestCase");
	}

	@Test
	void orderAnnotation() {
		var tests = executeTests(ClassOrderer.OrderAnnotation.class);

		tests.assertStatistics(stats -> stats.succeeded(callSequence.size()));

		assertThat(callSequence)//
				.containsExactly("A_TestCase", "C_TestCase", "B_TestCase");
	}

	@Test
	void random() {
		var tests = executeTests(ClassOrderer.Random.class);

		tests.assertStatistics(stats -> stats.succeeded(callSequence.size()));
	}

	private Events executeTests(Class<? extends ClassOrderer> classOrderer) {
		// @formatter:off
		return EngineTestKit
			.engine("junit-jupiter")
			.configurationParameter(DEFAULT_TEST_CLASS_ORDER_PROPERTY_NAME, classOrderer.getName())
			.selectors(selectClass(A_TestCase.class), selectClass(B_TestCase.class), selectClass(C_TestCase.class))
			.execute()
			.testEvents();
		// @formatter:on
	}

	static abstract class BaseTestCase {

		@BeforeEach
		void trackInvocations(TestInfo testInfo) {
			var testClass = testInfo.getTestClass().get();

			callSequence.add(testClass.getSimpleName());
		}

		@Test
		void a() {
		}
	}

	@Order(2)
	@DisplayName("Z")
	static class A_TestCase extends BaseTestCase {
	}

	static class B_TestCase extends BaseTestCase {
	}

	@Order(10)
	@DisplayName("A")
	static class C_TestCase extends BaseTestCase {
	}

}
