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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;
import org.junit.jupiter.engine.AbstractJupiterTestEngineTests;
import org.junit.jupiter.engine.execution.injection.sample.LongParameterResolver;

/**
 * Integration tests that verify support for extension registration via
 * {@link ExtendWith @ExtendWith} on annotations on parameters and fields.
 *
 * @since 5.8
 */
class ExtensionRegistrationViaParametersAndFieldsTests extends AbstractJupiterTestEngineTests {

	@Test
	void constructorParameter() {
		assertOneTestSucceeded(ConstructorParameterTestCase.class);
	}

	@Test
	void constructorParameterForNestedTestClass() {
		assertOneTestSucceeded(NestedConstructorParameterTestCase.class);
	}

	@Test
	void beforeAllMethodParameter() {
		assertOneTestSucceeded(BeforeAllParameterTestCase.class);
	}

	@Test
	void afterAllMethodParameter() {
		assertOneTestSucceeded(AfterAllParameterTestCase.class);
	}

	@Test
	void beforeEachMethodParameter() {
		assertOneTestSucceeded(BeforeEachParameterTestCase.class);
	}

	@Test
	void afterEachMethodParameter() {
		assertOneTestSucceeded(AfterEachParameterTestCase.class);
	}

	@Test
	void testMethodParameter() {
		assertOneTestSucceeded(TestMethodParameterTestCase.class);
	}

	@Test
	void testFactoryMethodParameter() {
		assertTestsSucceeded(TestFactoryMethodParameterTestCase.class, 2);
	}

	@Test
	void testTemplateMethodParameter() {
		assertTestsSucceeded(TestTemplateMethodParameterTestCase.class, 2);
	}

	private void assertOneTestSucceeded(Class<?> testClass) {
		assertTestsSucceeded(testClass, 1);
	}

	private void assertTestsSucceeded(Class<?> testClass, int expected) {
		executeTestsForClass(testClass).testEvents().assertStatistics(
			stats -> stats.started(expected).succeeded(expected).skipped(0).aborted(0).failed(0));
	}

	// -------------------------------------------------------------------

	/**
	 * The {@link MagicParameter.Extension} is first registered for the constructor
	 * and then used for lifecycle and test methods.
	 */
	@ExtendWith(LongParameterResolver.class)
	static class ConstructorParameterTestCase {

		ConstructorParameterTestCase(@MagicParameter("constructor") String text) {
			assertThat(text).isEqualTo("ConstructorParameterTestCase-0-constructor");
		}

		@BeforeEach
		void beforeEach(String text, TestInfo testInfo) {
			assertThat(text).isEqualTo("beforeEach-0-enigma");
			assertThat(testInfo).isNotNull();
		}

		@Test
		void test(TestInfo testInfo, String text) {
			assertThat(testInfo).isNotNull();
			assertThat(text).isEqualTo("test-1-enigma");
		}

		/**
		 * Redeclaring {@code @MagicParameter} should not result in a
		 * {@link ParameterResolutionException}.
		 */
		@AfterEach
		void afterEach(Long number, TestInfo testInfo, @MagicParameter("method") String text) {
			assertThat(number).isEqualTo(42L);
			assertThat(testInfo).isNotNull();
			assertThat(text).isEqualTo("afterEach-2-method");
		}

	}

	/**
	 * The {@link MagicParameter.Extension} is first registered for the constructor
	 * and then used for lifecycle and test methods.
	 */
	@Nested
	@ExtendWith(LongParameterResolver.class)
	class NestedConstructorParameterTestCase {

		NestedConstructorParameterTestCase(@MagicParameter("constructor") String text) {
			// Index is 1 instead of 0, since constructors for non-static nested classes
			// receive a reference to the enclosing instance as the first argument: this$0
			assertThat(text).isEqualTo("NestedConstructorParameterTestCase-1-constructor");
		}

		@BeforeEach
		void beforeEach(String text, TestInfo testInfo) {
			assertThat(text).isEqualTo("beforeEach-0-enigma");
			assertThat(testInfo).isNotNull();
		}

		@Test
		void test(TestInfo testInfo, String text) {
			assertThat(testInfo).isNotNull();
			assertThat(text).isEqualTo("test-1-enigma");
		}

		/**
		 * Redeclaring {@code @MagicParameter} should not result in a
		 * {@link ParameterResolutionException}.
		 */
		@AfterEach
		void afterEach(Long number, TestInfo testInfo, @MagicParameter("method") String text) {
			assertThat(number).isEqualTo(42L);
			assertThat(testInfo).isNotNull();
			assertThat(text).isEqualTo("afterEach-2-method");
		}

	}

	/**
	 * The {@link MagicParameter.Extension} is first registered for the {@code @BeforeAll}
	 * method and then used for other lifecycle methods and test methods.
	 */
	@ExtendWith(LongParameterResolver.class)
	static class BeforeAllParameterTestCase {

		@BeforeAll
		static void beforeAll(@MagicParameter("method") String text, TestInfo testInfo) {
			assertThat(text).isEqualTo("beforeAll-0-method");
			assertThat(testInfo).isNotNull();
		}

		@BeforeEach
		void beforeEach(String text, TestInfo testInfo) {
			assertThat(text).isEqualTo("beforeEach-0-enigma");
			assertThat(testInfo).isNotNull();
		}

		@Test
		void test(TestInfo testInfo, String text) {
			assertThat(testInfo).isNotNull();
			assertThat(text).isEqualTo("test-1-enigma");
		}

		/**
		 * Redeclaring {@code @MagicParameter} should not result in a
		 * {@link ParameterResolutionException}.
		 */
		@AfterEach
		void afterEach(Long number, TestInfo testInfo, @MagicParameter("method") String text) {
			assertThat(number).isEqualTo(42L);
			assertThat(testInfo).isNotNull();
			assertThat(text).isEqualTo("afterEach-2-method");
		}

		@AfterAll
		static void afterAll(String text, TestInfo testInfo) {
			assertThat(text).isEqualTo("afterAll-0-enigma");
			assertThat(testInfo).isNotNull();
		}

	}

	/**
	 * The {@link MagicParameter.Extension} is first registered for the {@code @AfterAll} method.
	 */
	@ExtendWith(LongParameterResolver.class)
	static class AfterAllParameterTestCase {

		@Test
		void test(TestInfo testInfo, String text) {
			assertThat(testInfo).isNotNull();
			assertThat(text).isEqualTo("test-1-enigma");
		}

		@AfterAll
		static void afterAll(Long number, TestInfo testInfo, @MagicParameter("method") String text) {
			assertThat(number).isEqualTo(42L);
			assertThat(testInfo).isNotNull();
			assertThat(text).isEqualTo("afterAll-2-method");
		}

	}

	/**
	 * The {@link MagicParameter.Extension} is first registered for the {@code @BeforeEach}
	 * method and then used for other lifecycle methods and test methods.
	 */
	@ExtendWith(LongParameterResolver.class)
	static class BeforeEachParameterTestCase {

		@BeforeEach
		void beforeEach(@MagicParameter("method") String text, TestInfo testInfo) {
			assertThat(text).isEqualTo("beforeEach-0-method");
			assertThat(testInfo).isNotNull();
		}

		@Test
		void test(TestInfo testInfo, String text) {
			assertThat(testInfo).isNotNull();
			assertThat(text).isEqualTo("test-1-enigma");
		}

		/**
		 * Redeclaring {@code @MagicParameter} should not result in a
		 * {@link ParameterResolutionException}.
		 */
		@AfterEach
		void afterEach(Long number, TestInfo testInfo, @MagicParameter("method") String text) {
			assertThat(number).isEqualTo(42L);
			assertThat(testInfo).isNotNull();
			assertThat(text).isEqualTo("afterEach-2-method");
		}

	}

	/**
	 * The {@link MagicParameter.Extension} is first registered for the {@code @AfterEach} method.
	 */
	@ExtendWith(LongParameterResolver.class)
	static class AfterEachParameterTestCase {

		@Test
		void test() {
		}

		@AfterEach
		void afterEach(Long number, TestInfo testInfo, @MagicParameter("method") String text) {
			assertThat(number).isEqualTo(42L);
			assertThat(testInfo).isNotNull();
			assertThat(text).isEqualTo("afterEach-2-method");
		}

	}

	/**
	 * The {@link MagicParameter.Extension} is first registered for the {@code @Test}
	 * method and then used for after-each lifecycle methods.
	 */
	@ExtendWith(LongParameterResolver.class)
	static class TestMethodParameterTestCase {

		@Test
		void test(TestInfo testInfo, @MagicParameter("method") String text) {
			assertThat(testInfo).isNotNull();
			assertThat(text).isEqualTo("test-1-method");
		}

		/**
		 * Redeclaring {@code @MagicParameter} should not result in a
		 * {@link ParameterResolutionException}.
		 */
		@AfterEach
		void afterEach(Long number, TestInfo testInfo, String text) {
			assertThat(number).isEqualTo(42L);
			assertThat(testInfo).isNotNull();
			assertThat(text).isEqualTo("afterEach-2-enigma");
		}

	}

	/**
	 * The {@link MagicParameter.Extension} is first registered for the {@code @TestFactory}
	 * method and then used for after-each lifecycle methods.
	 */
	@ExtendWith(LongParameterResolver.class)
	static class TestFactoryMethodParameterTestCase {

		@TestFactory
		Stream<DynamicTest> testFactory(TestInfo testInfo, @MagicParameter("method") String text) {
			assertThat(testInfo).isNotNull();
			assertThat(text).isEqualTo("testFactory-1-method");

			return IntStream.of(2, 4).mapToObj(num -> dynamicTest("" + num, () -> assertTrue(num % 2 == 0)));
		}

		/**
		 * Redeclaring {@code @MagicParameter} should not result in a
		 * {@link ParameterResolutionException}.
		 */
		@AfterEach
		void afterEach(Long number, TestInfo testInfo, String text) {
			assertThat(number).isEqualTo(42L);
			assertThat(testInfo).isNotNull();
			assertThat(text).isEqualTo("afterEach-2-enigma");
		}

	}

	/**
	 * The {@link MagicParameter.Extension} is first registered for the {@code @TestTemplate}
	 * method and then used for after-each lifecycle methods.
	 */
	@ExtendWith(LongParameterResolver.class)
	static class TestTemplateMethodParameterTestCase {

		@TestTemplate
		@ExtendWith(TwoInvocationsContextProvider.class)
		void testTemplate(TestInfo testInfo, @MagicParameter("method") String text) {
			assertThat(testInfo).isNotNull();
			assertThat(text).isEqualTo("testTemplate-1-method");
		}

		/**
		 * Redeclaring {@code @MagicParameter} should not result in a
		 * {@link ParameterResolutionException}.
		 */
		@AfterEach
		void afterEach(Long number, TestInfo testInfo, String text) {
			assertThat(number).isEqualTo(42L);
			assertThat(testInfo).isNotNull();
			assertThat(text).isEqualTo("afterEach-2-enigma");
		}

	}

	private static class TwoInvocationsContextProvider implements TestTemplateInvocationContextProvider {

		@Override
		public boolean supportsTestTemplate(ExtensionContext context) {
			return true;
		}

		@Override
		public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
			return Stream.of(emptyTestTemplateInvocationContext(), emptyTestTemplateInvocationContext());
		}

		private static TestTemplateInvocationContext emptyTestTemplateInvocationContext() {
			return new TestTemplateInvocationContext() {
			};
		}
	}

}

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(MagicParameter.Extension.class)
@interface MagicParameter {

	String value();

	class Extension implements ParameterResolver {

		@Override
		public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
			return parameterContext.getParameter().getType() == String.class;
		}

		@Override
		public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
			String text = parameterContext.findAnnotation(MagicParameter.class)//
					.map(MagicParameter::value)//
					.orElse("enigma");
			Executable declaringExecutable = parameterContext.getDeclaringExecutable();
			String name = declaringExecutable instanceof Constructor
					? declaringExecutable.getDeclaringClass().getSimpleName()
					: declaringExecutable.getName();
			return String.format("%s-%d-%s", name, parameterContext.getIndex(), text);
		}
	}

}
