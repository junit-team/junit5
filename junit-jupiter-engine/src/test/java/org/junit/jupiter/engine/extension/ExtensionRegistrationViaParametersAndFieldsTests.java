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

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.junit.platform.commons.util.AnnotationUtils.findAnnotatedFields;
import static org.junit.platform.commons.util.ReflectionUtils.makeAccessible;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.LogRecord;
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
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;
import org.junit.jupiter.api.fixtures.TrackLogRecords;
import org.junit.jupiter.engine.AbstractJupiterTestEngineTests;
import org.junit.jupiter.engine.execution.injection.sample.LongParameterResolver;
import org.junit.platform.commons.logging.LogRecordListener;
import org.junit.platform.commons.util.ExceptionUtils;
import org.junit.platform.commons.util.ReflectionUtils;

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
		assertTestsSucceeded(NestedConstructorParameterTestCase.class, 2);
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

	@Test
	void staticField() {
		assertOneTestSucceeded(StaticFieldTestCase.class);
	}

	@Test
	void instanceField() {
		assertOneTestSucceeded(InstanceFieldTestCase.class);
	}

	@Test
	void fieldsWithTestInstancePerClass() {
		assertOneTestSucceeded(TestInstancePerClassFieldTestCase.class);
	}

	@Test
	@TrackLogRecords
	void registrationOrder(LogRecordListener listener) {
		assertOneTestSucceeded(AllInOneWithTestInstancePerMethodTestCase.class);
		assertThat(getRegisteredLocalExtensions(listener))//
				.containsExactly("StaticField", "ConstructorParameter", "BeforeAllParameter", "BeforeEachParameter",
					"AfterEachParameter", "AfterAllParameter", "TestParameter", "InstanceField");

		listener.clear();
		assertOneTestSucceeded(AllInOneWithTestInstancePerClassTestCase.class);
		assertThat(getRegisteredLocalExtensions(listener))//
				.containsExactly("StaticField", "ConstructorParameter", "BeforeAllParameter", "BeforeEachParameter",
					"AfterEachParameter", "AfterAllParameter", "InstanceField", "TestParameter");
	}

	private List<String> getRegisteredLocalExtensions(LogRecordListener listener) {
		// @formatter:off
		return listener.stream(MutableExtensionRegistry.class, Level.FINER)
			.map(LogRecord::getMessage)
			.filter(message -> message.contains("local extension"))
			.map(message -> message.substring(message.lastIndexOf('.') + 1, message.indexOf("$")))
			.collect(toList());
		// @formatter:on
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

		NestedConstructorParameterTestCase(TestInfo testInfo, @MagicParameter("constructor") String text) {
			assertThat(testInfo).isNotNull();
			// Index is 2 instead of 1, since constructors for non-static nested classes
			// receive a reference to the enclosing instance as the first argument: this$0
			assertThat(text).isEqualTo("NestedConstructorParameterTestCase-2-constructor");
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

		@Nested
		class DoublyNestedConstructorParameterTestCase {

			DoublyNestedConstructorParameterTestCase(TestInfo testInfo, String text) {
				assertThat(testInfo).isNotNull();
				// Index is 2 instead of 1, since constructors for non-static nested classes
				// receive a reference to the enclosing instance as the first argument: this$0
				assertThat(text).isEqualTo("DoublyNestedConstructorParameterTestCase-2-enigma");
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

	/**
	 * The {@link MagicField.Extension} is registered via a static field.
	 */
	static class StaticFieldTestCase {

		@MagicField
		static String staticField1;

		@MagicField
		static String staticField2;

		@BeforeAll
		static void beforeAll() {
			assertThat(staticField1).isEqualTo("beforeAll - staticField1");
			assertThat(staticField2).isEqualTo("beforeAll - staticField2");
		}

		@Test
		void test() {
			assertThat(staticField1).isEqualTo("beforeAll - staticField1");
			assertThat(staticField2).isEqualTo("beforeAll - staticField2");
		}
	}

	/**
	 * The {@link MagicField.Extension} is registered via an instance field.
	 */
	static class InstanceFieldTestCase {

		@MagicField
		String instanceField1;

		@MagicField
		String instanceField2;

		@Test
		void test() {
			assertThat(instanceField1).isEqualTo("beforeEach - instanceField1");
			assertThat(instanceField2).isEqualTo("beforeEach - instanceField2");
		}
	}

	/**
	 * The {@link MagicField.Extension} is registered via a static field and
	 * an instance field.
	 */
	@TestInstance(Lifecycle.PER_CLASS)
	static class TestInstancePerClassFieldTestCase {

		@MagicField
		static String staticField;

		@MagicField
		String instanceField;

		@BeforeAll
		void beforeAll() {
			assertThat(staticField).isEqualTo("beforeAll - staticField");
			assertThat(instanceField).isNull();
		}

		@Test
		void test() {
			assertThat(staticField).isEqualTo("beforeAll - staticField");
			assertThat(instanceField).isEqualTo("beforeEach - instanceField");
		}
	}

	@TestInstance(Lifecycle.PER_METHOD)
	static class AllInOneWithTestInstancePerMethodTestCase {

		@StaticField
		static String staticField;

		@InstanceField
		String instanceField;

		AllInOneWithTestInstancePerMethodTestCase(@ConstructorParameter String text) {
			assertThat(text).isEqualTo("enigma");
		}

		@BeforeAll
		static void beforeAll(@BeforeAllParameter String text) {
			assertThat(text).isEqualTo("enigma");
			assertThat(staticField).isEqualTo("beforeAll - staticField");
		}

		@BeforeEach
		void beforeEach(@BeforeEachParameter String text) {
			assertThat(text).isEqualTo("enigma");
			assertThat(staticField).isEqualTo("beforeAll - staticField");
			assertThat(instanceField).isEqualTo("beforeEach - instanceField");
		}

		@Test
		void test(@TestParameter String text) {
			assertThat(text).isEqualTo("enigma");
			assertThat(staticField).isEqualTo("beforeAll - staticField");
			assertThat(instanceField).isEqualTo("beforeEach - instanceField");
		}

		@AfterEach
		void afterEach(@AfterEachParameter String text) {
			assertThat(text).isEqualTo("enigma");
			assertThat(staticField).isEqualTo("beforeAll - staticField");
			assertThat(instanceField).isEqualTo("beforeEach - instanceField");
		}

		@AfterAll
		static void afterAll(@AfterAllParameter String text) {
			assertThat(text).isEqualTo("enigma");
			assertThat(staticField).isEqualTo("beforeAll - staticField");
		}

	}

	@TestInstance(Lifecycle.PER_CLASS)
	static class AllInOneWithTestInstancePerClassTestCase extends AllInOneWithTestInstancePerMethodTestCase {

		AllInOneWithTestInstancePerClassTestCase(@ConstructorParameter String text) {
			super(text);
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

@SuppressWarnings("unused")
class BaseParameterExtension<T extends Annotation> implements ParameterResolver {

	private final Class<T> annotationType;

	@SuppressWarnings("unchecked")
	BaseParameterExtension() {
		Type genericSuperclass = getClass().getGenericSuperclass();
		this.annotationType = (Class<T>) ((ParameterizedType) genericSuperclass).getActualTypeArguments()[0];
	}

	@Override
	public final boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		return parameterContext.isAnnotated(this.annotationType)
				&& parameterContext.getParameter().getType() == String.class;
	}

	@Override
	public final Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		return "enigma";
	}
}

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(ConstructorParameter.Extension.class)
@interface ConstructorParameter {
	class Extension extends BaseParameterExtension<ConstructorParameter> {
	}
}

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(BeforeAllParameter.Extension.class)
@interface BeforeAllParameter {
	class Extension extends BaseParameterExtension<BeforeAllParameter> {
	}
}

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(AfterAllParameter.Extension.class)
@interface AfterAllParameter {
	class Extension extends BaseParameterExtension<AfterAllParameter> {
	}
}

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(BeforeEachParameter.Extension.class)
@interface BeforeEachParameter {
	class Extension extends BaseParameterExtension<BeforeEachParameter> {
	}
}

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(AfterEachParameter.Extension.class)
@interface AfterEachParameter {
	class Extension extends BaseParameterExtension<AfterEachParameter> {
	}
}

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(TestParameter.Extension.class)
@interface TestParameter {
	class Extension extends BaseParameterExtension<TestParameter> {
	}
}

class BaseFieldExtension<T extends Annotation> implements BeforeAllCallback, BeforeEachCallback {

	private final Class<T> annotationType;

	@SuppressWarnings("unchecked")
	BaseFieldExtension() {
		Type genericSuperclass = getClass().getGenericSuperclass();
		this.annotationType = (Class<T>) ((ParameterizedType) genericSuperclass).getActualTypeArguments()[0];
	}

	@Override
	public final void beforeAll(ExtensionContext context) throws Exception {
		injectFields("beforeAll", context.getRequiredTestClass(), null, ReflectionUtils::isStatic);
	}

	@Override
	public final void beforeEach(ExtensionContext context) throws Exception {
		injectFields("beforeEach", context.getRequiredTestClass(), context.getRequiredTestInstance(),
			ReflectionUtils::isNotStatic);
	}

	private void injectFields(String trigger, Class<?> testClass, Object instance, Predicate<Field> predicate) {
		findAnnotatedFields(testClass, this.annotationType, predicate).forEach(field -> {
			try {
				makeAccessible(field).set(instance, trigger + " - " + field.getName());
			}
			catch (Throwable t) {
				ExceptionUtils.throwAsUncheckedException(t);
			}
		});
	}
}

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(MagicField.Extension.class)
@interface MagicField {
	class Extension extends BaseFieldExtension<MagicField> {
	}
}

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(InstanceField.Extension.class)
@interface InstanceField {
	class Extension extends BaseFieldExtension<InstanceField> {
	}
}

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(StaticField.Extension.class)
@interface StaticField {
	class Extension extends BaseFieldExtension<StaticField> {
	}
}
