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

import static org.assertj.core.api.Assertions.allOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.container;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.event;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.finishedWithFailure;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.test;
import static org.junit.platform.engine.test.event.TestExecutionResultConditions.hasCause;
import static org.junit.platform.engine.test.event.TestExecutionResultConditions.isA;
import static org.junit.platform.engine.test.event.TestExecutionResultConditions.message;

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Predicate;

import org.assertj.core.api.Condition;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.junit.jupiter.engine.AbstractJupiterTestEngineTests;
import org.junit.jupiter.engine.JupiterTestEngine;
import org.junit.platform.commons.util.AnnotationUtils;
import org.junit.platform.commons.util.ExceptionUtils;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.engine.test.event.ExecutionEvent;
import org.junit.platform.engine.test.event.ExecutionEventRecorder;

/**
 * Integration tests that verify support for programmatic extension registration
 * via {@link RegisterExtension @RegisterExtension} in the {@link JupiterTestEngine}.
 *
 * @since 5.1
 */
class ProgrammaticExtensionRegistrationTests extends AbstractJupiterTestEngineTests {

	@Test
	void instanceLevel() {
		assertOneTestSucceeded(InstanceLevelExtensionRegistrationTestCase.class);
	}

	@Test
	void instanceLevelWithInjectedExtension() {
		assertOneTestSucceeded(InstanceLevelExtensionRegistrationWithInjectedExtensionTestCase.class);
	}

	@Test
	void instanceLevelWithTestInstancePerClassLifecycle() {
		assertOneTestSucceeded(InstanceLevelExtensionRegistrationWithTestInstancePerClassLifecycleTestCase.class);
	}

	@Test
	void classLevel() {
		assertOneTestSucceeded(ClassLevelExtensionRegistrationTestCase.class);
	}

	@Test
	void classLevelFromSuperclass() {
		assertOneTestSucceeded(SubClassLevelExtensionRegistrationTestCase.class);
	}

	@Test
	void classLevelFromInterface() {
		assertOneTestSucceeded(ExtensionRegistrationFromInterfaceTestCase.class);
	}

	@Test
	void propagatesCheckedExceptionThrownDuringInitializationOfStaticField() {
		assertClassFails(ClassLevelExplosiveCheckedExceptionTestCase.class,
			allOf(isA(ExceptionInInitializerError.class), hasCause(allOf(isA(Exception.class), message("boom")))));
	}

	@Test
	void propagatesUncheckedExceptionThrownDuringInitializationOfStaticField() {
		assertClassFails(ClassLevelExplosiveUncheckedExceptionTestCase.class, allOf(
			isA(ExceptionInInitializerError.class), hasCause(allOf(isA(RuntimeException.class), message("boom")))));
	}

	@Test
	void propagatesErrorThrownDuringInitializationOfStaticField() {
		assertClassFails(ClassLevelExplosiveErrorTestCase.class, allOf(isA(Error.class), message("boom")));
	}

	@Test
	void propagatesCheckedExceptionThrownDuringInitializationOfInstanceField() {
		assertTestFails(InstanceLevelExplosiveCheckedExceptionTestCase.class,
			allOf(isA(Exception.class), message("boom")));
	}

	@Test
	void propagatesUncheckedExceptionThrownDuringInitializationOfInstanceField() {
		assertTestFails(InstanceLevelExplosiveUncheckedExceptionTestCase.class,
			allOf(isA(RuntimeException.class), message("boom")));
	}

	@Test
	void propagatesErrorThrownDuringInitializationOfInstanceField() {
		assertTestFails(InstanceLevelExplosiveErrorTestCase.class, allOf(isA(Error.class), message("boom")));
	}

	private void assertClassFails(Class<?> testClass, Condition<Throwable> causeCondition) {
		List<ExecutionEvent> executionEvents = executeTestsForClass(testClass).getExecutionEvents();
		assertThat(executionEvents) //
				.haveExactly(1, event(container(), finishedWithFailure(causeCondition)));
	}

	private void assertTestFails(Class<?> testClass, Condition<Throwable> causeCondition) {
		List<ExecutionEvent> executionEvents = executeTestsForClass(testClass).getExecutionEvents();
		assertThat(executionEvents) //
				.haveExactly(1, event(test(), finishedWithFailure(causeCondition)));
	}

	private void assertOneTestSucceeded(Class<?> testClass) {
		ExecutionEventRecorder eventRecorder = executeTestsForClass(testClass);
		assertAll(//
			() -> assertEquals(1, eventRecorder.getTestStartedCount(), "# tests started"), //
			() -> assertEquals(1, eventRecorder.getTestSuccessfulCount(), "# tests succeeded"), //
			() -> assertEquals(0, eventRecorder.getTestSkippedCount(), "# tests skipped"), //
			() -> assertEquals(0, eventRecorder.getTestAbortedCount(), "# tests aborted"), //
			() -> assertEquals(0, eventRecorder.getTestFailedCount(), "# tests failed")//
		);
	}

	// -------------------------------------------------------------------

	private static void assertWisdom(CrystalBall crystalBall, String wisdom, String useCase) {
		assertNotNull(crystalBall, useCase);
		assertEquals("Outlook good", wisdom, useCase);
	}

	static class InstanceLevelExtensionRegistrationTestCase {

		@RegisterExtension
		final CrystalBall crystalBall = new CrystalBall("Outlook good");

		@BeforeEach
		void beforeEach(String wisdom) {
			assertWisdom(crystalBall, wisdom, "@BeforeEach");
		}

		@Test
		void test(String wisdom) {
			assertWisdom(crystalBall, wisdom, "@Test");
		}

		@AfterEach
		void afterEach(String wisdom) {
			assertWisdom(crystalBall, wisdom, "@AfterEach");
		}

	}

	@ExtendWith(ExtensionInjector.class)
	static class InstanceLevelExtensionRegistrationWithInjectedExtensionTestCase {

		@RegisterExtension
		protected CrystalBall crystalBall; // Injected by ExtensionInjector.

		@BeforeEach
		void beforeEach(String wisdom) {
			assertWisdom(crystalBall, wisdom, "@BeforeEach");
		}

		@Test
		void test(String wisdom) {
			assertWisdom(crystalBall, wisdom, "@Test");
		}

		@AfterEach
		void afterEach(String wisdom) {
			assertWisdom(crystalBall, wisdom, "@AfterEach");
		}

	}

	@TestInstance(PER_CLASS)
	static class InstanceLevelExtensionRegistrationWithTestInstancePerClassLifecycleTestCase {

		@RegisterExtension
		final CrystalBall crystalBall = new CrystalBall("Outlook good");

		@BeforeAll
		void beforeAll(String wisdom) {
			assertWisdom(crystalBall, wisdom, "@BeforeAll");
		}

		@BeforeEach
		void beforeEach(String wisdom) {
			assertWisdom(crystalBall, wisdom, "@BeforeEach");
		}

		@Test
		void test(String wisdom) {
			assertWisdom(crystalBall, wisdom, "@Test");
		}

		@AfterEach
		void afterEach(String wisdom) {
			assertWisdom(crystalBall, wisdom, "@AfterEach");
		}

		@AfterAll
		void afterAll(String wisdom) {
			assertWisdom(crystalBall, wisdom, "@AfterAll");
		}

	}

	static class ClassLevelExtensionRegistrationTestCase {

		@RegisterExtension
		static final CrystalBall crystalBall = new CrystalBall("Outlook good");

		@BeforeAll
		static void beforeAll(String wisdom) {
			assertWisdom(crystalBall, wisdom, "@BeforeAll");
		}

		@BeforeEach
		void beforeEach(String wisdom) {
			assertWisdom(crystalBall, wisdom, "@BeforeEach");
		}

		@Test
		void test(String wisdom) {
			assertWisdom(crystalBall, wisdom, "@Test");
		}

		@AfterEach
		void afterEach(String wisdom) {
			assertWisdom(crystalBall, wisdom, "@AfterEach");
		}

		@AfterAll
		static void afterAll(String wisdom) {
			assertWisdom(crystalBall, wisdom, "@AfterAll");
		}

	}

	static class SubClassLevelExtensionRegistrationTestCase extends ClassLevelExtensionRegistrationTestCase {

		@Test
		@Override
		void test(String wisdom) {
			assertWisdom(crystalBall, wisdom, "Overridden @Test");
		}

	}

	interface ClassLevelExtensionRegistrationInterface {

		@RegisterExtension
		static final CrystalBall crystalBall = new CrystalBall("Outlook good");

		@BeforeAll
		static void beforeAll(String wisdom) {
			assertWisdom(crystalBall, wisdom, "@BeforeAll");
		}

		@BeforeEach
		default void beforeEach(String wisdom) {
			assertWisdom(crystalBall, wisdom, "@BeforeEach");
		}

		@AfterEach
		default void afterEach(String wisdom) {
			assertWisdom(crystalBall, wisdom, "@AfterEach");
		}

		@AfterAll
		static void afterAll(String wisdom) {
			assertWisdom(crystalBall, wisdom, "@AfterAll");
		}

	}

	static class ExtensionRegistrationFromInterfaceTestCase implements ClassLevelExtensionRegistrationInterface {

		@Test
		void test(String wisdom) {
			assertWisdom(crystalBall, wisdom, "@Test");
		}

	}

	private static class CrystalBall implements ParameterResolver {

		private final String wisdom;

		public CrystalBall(String wisdom) {
			this.wisdom = wisdom;
		}

		@Override
		public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
			return parameterContext.getParameter().getType() == String.class;
		}

		@Override
		public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
			return this.wisdom;
		}

	}

	static class AbstractTestCase {

		@Test
		void test() {
		}

	}

	static class ClassLevelExplosiveCheckedExceptionTestCase extends AbstractTestCase {

		@RegisterExtension
		static Extension field = new ExplosiveExtension(new Exception("boom"));

	}

	static class ClassLevelExplosiveUncheckedExceptionTestCase extends AbstractTestCase {

		@RegisterExtension
		static Extension field = new ExplosiveExtension(new RuntimeException("boom"));

	}

	static class ClassLevelExplosiveErrorTestCase extends AbstractTestCase {

		@RegisterExtension
		static Extension field = new ExplosiveExtension(new Error("boom"));

	}

	static class InstanceLevelExplosiveCheckedExceptionTestCase extends AbstractTestCase {

		@RegisterExtension
		Extension field = new ExplosiveExtension(new Exception("boom"));

	}

	static class InstanceLevelExplosiveUncheckedExceptionTestCase extends AbstractTestCase {

		@RegisterExtension
		Extension field = new ExplosiveExtension(new RuntimeException("boom"));

	}

	static class InstanceLevelExplosiveErrorTestCase extends AbstractTestCase {

		@RegisterExtension
		Extension field = new ExplosiveExtension(new Error("boom"));

	}

	static class ExplosiveExtension implements Extension {

		ExplosiveExtension(Throwable t) {
			throw ExceptionUtils.throwAsUncheckedException(t);
		}

	}

	/**
	 * Mimics a dependency injection framework such as Spring, Guice, CDI, etc.,
	 * where the instance of the extension registered via
	 * {@link RegisterExtension @RegisterExtension} is managed by the DI
	 * framework and injected into the test instance.
	 */
	private static class ExtensionInjector implements TestInstancePostProcessor {

		private static final Predicate<Field> isCrystalBall = field -> CrystalBall.class.isAssignableFrom(
			field.getType());

		@Override
		public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
			// @formatter:off
			AnnotationUtils.findAnnotatedFields(testInstance.getClass(), RegisterExtension.class, isCrystalBall).stream()
				.findFirst()
				.ifPresent(field -> {
					try {
						ReflectionUtils.makeAccessible(field);
						field.set(testInstance, new CrystalBall("Outlook good"));
					}
					catch (Throwable t) {
						ExceptionUtils.throwAsUncheckedException(t);
					}
				});
			// @formatter:on
		}

	}

}
