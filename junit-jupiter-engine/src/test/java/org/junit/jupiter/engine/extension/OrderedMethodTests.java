/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.extension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.MethodOrderer.Alphanumeric;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.MethodOrderer.Random;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.TestReporter;
import org.junit.jupiter.engine.AbstractJupiterTestEngineTests;
import org.junit.jupiter.engine.JupiterTestEngine;
import org.junit.platform.commons.util.ClassUtils;

/**
 * Integration tests that verify support for custom test method execution order
 * in the {@link JupiterTestEngine}.
 *
 * @since 5.4
 */
class OrderedMethodTests extends AbstractJupiterTestEngineTests {

	// TODO Test concurrent execution.

	private static final Set<String> callSequence = new LinkedHashSet<>();

	@BeforeEach
	void clearCallSequence() {
		callSequence.clear();
	}

	@Test
	void alphanumeric() {
		Class<?> testClass = AlphanumericTestCase.class;

		// The name of the base class MUST start with a letter alphanumerically
		// greater than "A" so that BaseTestCase comes after AlphanumericTestCase
		// if methods are sorted by class name for the fallback ordering if two
		// methods have the same name but different parameter lists. Note, however,
		// that Alphanumeric actually does not order methods like that, but we want
		// this check to remain in place to ensure that the ordering does not rely
		// on the class names.
		assertThat(testClass.getSuperclass().getName()).isGreaterThan(testClass.getName());

		var tests = executeTestsForClass(AlphanumericTestCase.class).tests();

		tests.assertStatistics(stats -> stats.succeeded(callSequence.size()));

		assertThat(callSequence).containsExactly("$()", "AAA()", "AAA(org.junit.jupiter.api.TestInfo)",
			"AAA(org.junit.jupiter.api.TestReporter)", "ZZ_Top()", "___()", "a1()", "a2()", "b()", "c()", "zzz()");
	}

	@Test
	void orderAnnotation() {
		assertOrderAnnotationSupport(OrderAnnotationTestCase.class);
	}

	@Test
	void orderAnnotationInNestedTestClass() {
		assertOrderAnnotationSupport(OuterTestCase.class);
	}

	private void assertOrderAnnotationSupport(Class<?> testClass) {
		var tests = executeTestsForClass(testClass).tests();

		tests.assertStatistics(stats -> stats.succeeded(callSequence.size()));

		assertThat(callSequence).containsExactly("test1", "test2", "test3", "test4", "test5", "test6");
	}

	@Test
	void random() {
		Set<String> uniqueSequences = new HashSet<>();

		for (int i = 0; i < 10; i++) {
			callSequence.clear();

			var tests = executeTestsForClass(RandomTestCase.class).tests();

			tests.assertStatistics(stats -> stats.succeeded(callSequence.size()));

			uniqueSequences.add(callSequence.stream().collect(Collectors.joining(",")));
		}

		// We assume that at least 3 out of 10 are different...
		assertThat(uniqueSequences.size()).isGreaterThanOrEqualTo(3);
	}

	// -------------------------------------------------------------------------

	static class BaseTestCase {

		@Test
		void AAA() {
		}

		@Test
		void c() {
		}

	}

	@TestMethodOrder(Alphanumeric.class)
	static class AlphanumericTestCase extends BaseTestCase {

		@BeforeEach
		void trackInvocations(TestInfo testInfo) {
			var method = testInfo.getTestMethod().get();
			var signature = String.format("%s(%s)", method.getName(),
				ClassUtils.nullSafeToString(method.getParameterTypes()));

			callSequence.add(signature);
		}

		@TestFactory
		DynamicTest b() {
			return dynamicTest("dynamic", () -> {
			});
		}

		@Test
		void $() {
		}

		@Test
		void ___() {
		}

		@Test
		void AAA(TestReporter testReporter) {
		}

		@Test
		void AAA(TestInfo testInfo) {
		}

		@Test
		void ZZ_Top() {
		}

		@Test
		void a1() {
		}

		@Test
		void a2() {
		}

		@RepeatedTest(1)
		void zzz() {
		}
	}

	@TestMethodOrder(OrderAnnotation.class)
	static class OrderAnnotationTestCase {

		@BeforeEach
		void trackInvocations(TestInfo testInfo) {
			callSequence.add(testInfo.getDisplayName());
		}

		@Test
		@DisplayName("test6")
		// @Order(6)
		void defaultOrderValue() {
		}

		@Test
		@DisplayName("test3")
		@Order(3)
		void $() {
		}

		@Test
		@DisplayName("test5")
		@Order(5)
		void AAA() {
		}

		@TestFactory
		@DisplayName("test4")
		@Order(4)
		DynamicTest aaa() {
			return dynamicTest("test4", () -> {
			});
		}

		@Test
		@DisplayName("test1")
		@Order(1)
		void zzz() {
		}

		@RepeatedTest(value = 1, name = "{displayName}")
		@DisplayName("test2")
		@Order(2)
		void ___() {
		}
	}

	static class OuterTestCase {

		@Nested
		class NestedOrderAnnotationTestCase extends OrderAnnotationTestCase {
		}
	}

	@TestMethodOrder(Random.class)
	static class RandomTestCase {

		@BeforeEach
		void trackInvocations(TestInfo testInfo) {
			callSequence.add(testInfo.getDisplayName());
		}

		@Test
		void test1() {
		}

		@Test
		void test2() {
		}

		@Test
		void test3() {
		}

		@TestFactory
		DynamicTest test4() {
			return dynamicTest("dynamic", () -> {
			});
		}

		@RepeatedTest(1)
		void test5() {
		}
	}

}
