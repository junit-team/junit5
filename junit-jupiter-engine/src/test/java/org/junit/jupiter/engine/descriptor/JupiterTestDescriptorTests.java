/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine.descriptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.engine.descriptor.JupiterTestDescriptorTests.StaticTestCase.StaticTestCaseLevel2;
import org.junit.platform.engine.TestTag;
import org.junit.platform.engine.UniqueId;

/**
 * Unit tests for {@link ClassTestDescriptor}, {@link NestedClassTestDescriptor},
 * and {@link TestMethodTestDescriptor}.
 *
 * @since 5.0
 * @see org.junit.jupiter.engine.descriptor.LifecycleMethodUtilsTests
 * @see org.junit.jupiter.engine.InvalidLifecycleMethodConfigurationTests
 */
class JupiterTestDescriptorTests {

	private static final UniqueId uniqueId = UniqueId.root("enigma", "foo");

	@Test
	void constructFromClass() throws Exception {
		ClassTestDescriptor descriptor = new ClassTestDescriptor(uniqueId, TestCase.class);

		assertEquals(TestCase.class, descriptor.getTestClass());
		assertThat(descriptor.getTags()).containsExactly(TestTag.create("classTag1"), TestTag.create("classTag2"));
	}

	@Test
	void constructFromClassWithInvalidBeforeAllDeclaration() throws Exception {
		// Note: if we can instantiate the descriptor, then the invalid configuration
		// will not be reported during the test engine discovery phase.
		ClassTestDescriptor descriptor = new ClassTestDescriptor(uniqueId, TestCaseWithInvalidBeforeAllMethod.class);

		assertEquals(TestCaseWithInvalidBeforeAllMethod.class, descriptor.getTestClass());
	}

	@Test
	void constructFromClassWithInvalidAfterAllDeclaration() throws Exception {
		// Note: if we can instantiate the descriptor, then the invalid configuration
		// will not be reported during the test engine discovery phase.
		ClassTestDescriptor descriptor = new ClassTestDescriptor(uniqueId, TestCaseWithInvalidAfterAllMethod.class);

		assertEquals(TestCaseWithInvalidAfterAllMethod.class, descriptor.getTestClass());
	}

	@Test
	void constructFromClassWithInvalidBeforeEachDeclaration() throws Exception {
		// Note: if we can instantiate the descriptor, then the invalid configuration
		// will not be reported during the test engine discovery phase.
		ClassTestDescriptor descriptor = new ClassTestDescriptor(uniqueId, TestCaseWithInvalidBeforeEachMethod.class);

		assertEquals(TestCaseWithInvalidBeforeEachMethod.class, descriptor.getTestClass());
	}

	@Test
	void constructFromClassWithInvalidAfterEachDeclaration() throws Exception {
		// Note: if we can instantiate the descriptor, then the invalid configuration
		// will not be reported during the test engine discovery phase.
		ClassTestDescriptor descriptor = new ClassTestDescriptor(uniqueId, TestCaseWithInvalidAfterEachMethod.class);

		assertEquals(TestCaseWithInvalidAfterEachMethod.class, descriptor.getTestClass());
	}

	@Test
	void constructFromMethod() throws Exception {
		Class<?> testClass = TestCase.class;
		Method testMethod = testClass.getDeclaredMethod("test");
		TestMethodTestDescriptor descriptor = new TestMethodTestDescriptor(uniqueId, testClass, testMethod);

		assertEquals(uniqueId, descriptor.getUniqueId());
		assertEquals(testMethod, descriptor.getTestMethod());
		assertEquals("test()", descriptor.getDisplayName(), "display name:");
	}

	@Test
	void constructFromMethodWithAnnotations() throws Exception {
		JupiterTestDescriptor classDescriptor = new ClassTestDescriptor(uniqueId, TestCase.class);
		Method testMethod = TestCase.class.getDeclaredMethod("foo");
		TestMethodTestDescriptor methodDescriptor = new TestMethodTestDescriptor(uniqueId, TestCase.class, testMethod);
		classDescriptor.addChild(methodDescriptor);

		assertEquals(testMethod, methodDescriptor.getTestMethod());
		assertEquals("custom test name", methodDescriptor.getDisplayName(), "display name:");

		List<String> tags = methodDescriptor.getTags().stream().map(TestTag::getName).collect(Collectors.toList());
		assertEquals(4, methodDescriptor.getTags().size());
		assertTrue(tags.contains("methodTag1"));
		assertTrue(tags.contains("methodTag2"));

		// Methods "inherit" tags from their test class
		assertTrue(tags.contains("classTag1"));
		assertTrue(tags.contains("classTag2"));
	}

	@Test
	void constructFromMethodWithCustomTestAnnotation() throws Exception {
		Method testMethod = TestCase.class.getDeclaredMethod("customTestAnnotation");
		TestMethodTestDescriptor descriptor = new TestMethodTestDescriptor(uniqueId, TestCase.class, testMethod);

		assertEquals(testMethod, descriptor.getTestMethod());
		assertEquals("custom name", descriptor.getDisplayName(), "display name:");
		assertThat(descriptor.getTags()).containsExactly(TestTag.create("custom-tag"));
	}

	@Test
	void constructFromMethodWithParameters() throws Exception {
		Method testMethod = TestCase.class.getDeclaredMethod("test", String.class, BigDecimal.class);
		TestMethodTestDescriptor descriptor = new TestMethodTestDescriptor(uniqueId, TestCase.class, testMethod);

		assertEquals(testMethod, descriptor.getTestMethod());
		assertEquals("test(String, BigDecimal)", descriptor.getDisplayName(), "display name");
	}

	@Test
	void constructFromMethodWithPrimitiveArrayParameter() throws Exception {
		Method testMethod = TestCase.class.getDeclaredMethod("test", int[].class);
		TestMethodTestDescriptor descriptor = new TestMethodTestDescriptor(uniqueId, TestCase.class, testMethod);

		assertEquals(testMethod, descriptor.getTestMethod());
		assertEquals("test(int[])", descriptor.getDisplayName(), "display name");
	}

	@Test
	void constructFromMethodWithObjectArrayParameter() throws Exception {
		Method testMethod = TestCase.class.getDeclaredMethod("test", String[].class);
		TestMethodTestDescriptor descriptor = new TestMethodTestDescriptor(uniqueId, TestCase.class, testMethod);

		assertEquals(testMethod, descriptor.getTestMethod());
		assertEquals("test(String[])", descriptor.getDisplayName(), "display name");
	}

	@Test
	void constructFromMethodWithMultidimensionalPrimitiveArrayParameter() throws Exception {
		Method testMethod = TestCase.class.getDeclaredMethod("test", int[][][][][].class);
		TestMethodTestDescriptor descriptor = new TestMethodTestDescriptor(uniqueId, TestCase.class, testMethod);

		assertEquals(testMethod, descriptor.getTestMethod());
		assertEquals("test(int[][][][][])", descriptor.getDisplayName(), "display name");
	}

	@Test
	void constructFromMethodWithMultidimensionalObjectArrayParameter() throws Exception {
		Method testMethod = TestCase.class.getDeclaredMethod("test", String[][][][][].class);
		TestMethodTestDescriptor descriptor = new TestMethodTestDescriptor(uniqueId, TestCase.class, testMethod);

		assertEquals(testMethod, descriptor.getTestMethod());
		assertEquals("test(String[][][][][])", descriptor.getDisplayName(), "display name");
	}

	@Test
	void defaultDisplayNamesForTestClasses() {
		ClassTestDescriptor descriptor = new ClassTestDescriptor(uniqueId, getClass());
		assertEquals(getClass().getSimpleName(), descriptor.getDisplayName());

		descriptor = new NestedClassTestDescriptor(uniqueId, NestedTestCase.class);
		assertEquals(NestedTestCase.class.getSimpleName(), descriptor.getDisplayName());

		descriptor = new ClassTestDescriptor(uniqueId, StaticTestCase.class);
		String staticDisplayName = getClass().getSimpleName() + "$" + StaticTestCase.class.getSimpleName();
		assertEquals(staticDisplayName, descriptor.getDisplayName());

		descriptor = new ClassTestDescriptor(uniqueId, StaticTestCaseLevel2.class);
		staticDisplayName += "$" + StaticTestCaseLevel2.class.getSimpleName();
		assertEquals(staticDisplayName, descriptor.getDisplayName());
	}

	// -------------------------------------------------------------------------

	@Test
	@DisplayName("custom name")
	@Tag("  custom-tag  ")
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	@interface CustomTestAnnotation {
	}

	@Tag("classTag1")
	@Tag("classTag2")
	@DisplayName("custom class name")
	@SuppressWarnings("unused")
	private static class TestCase {

		void test() {
		}

		void test(String txt, BigDecimal sum) {
		}

		void test(int[] nums) {
		}

		void test(int[][][][][] nums) {
		}

		void test(String[] info) {
		}

		void test(String[][][][][] info) {
		}

		@Test
		@DisplayName("custom test name")
		@Tag("methodTag1")
		@Tag("methodTag2")
		@Tag("tag containing whitespace")
		void foo() {
		}

		@CustomTestAnnotation
		void customTestAnnotation() {
		}

	}

	private static class TestCaseWithInvalidBeforeAllMethod {

		// must be static
		@BeforeAll
		void beforeAll() {
		}

		@Test
		void test() {
		}

	}

	private static class TestCaseWithInvalidAfterAllMethod {

		// must be static
		@AfterAll
		void afterAll() {
		}

		@Test
		void test() {
		}

	}

	private static class TestCaseWithInvalidBeforeEachMethod {

		// must NOT be static
		@BeforeEach
		static void beforeEach() {
		}

		@Test
		void test() {
		}

	}

	private static class TestCaseWithInvalidAfterEachMethod {

		// must NOT be static
		@AfterEach
		static void afterEach() {
		}

		@Test
		void test() {
		}

	}

	@Nested
	class NestedTestCase {
	}

	static class StaticTestCase {

		static class StaticTestCaseLevel2 {
		}
	}

}
