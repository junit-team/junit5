/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.extension;

import static java.lang.StackWalker.Option.RETAIN_CLASS_REFERENCE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_METHOD;
import static org.junit.platform.testkit.engine.EventConditions.finishedWithFailure;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.message;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AutoClose;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.fixtures.TrackLogRecords;
import org.junit.jupiter.engine.AbstractJupiterTestEngineTests;
import org.junit.platform.commons.logging.LogRecordListener;
import org.junit.platform.testkit.engine.Events;

/**
 * Integration tests for {@link AutoClose @AutoClose} and the {@link AutoCloseExtension}.
 *
 * @since 5.11
 */
class AutoCloseTests extends AbstractJupiterTestEngineTests {

	private static final List<String> recorder = new ArrayList<>();

	@BeforeEach
	@AfterEach
	void resetTracking() {
		InstancePerClassTestCase.closed = false;
		recorder.clear();
	}

	@Test
	void blankCloseMethodName() {
		Class<?> testClass = BlankCloseMethodNameTestCase.class;
		String msg = String.format("@AutoClose on field %s.field must specify a method name.",
			testClass.getCanonicalName());
		Events tests = executeTestsForClass(testClass).testEvents();
		assertFailingWithMessage(tests, msg);
	}

	@Test
	void primitiveTypeCannotBeClosed() {
		Class<?> testClass = PrimitiveFieldTestCase.class;
		String msg = String.format("@AutoClose is not supported on primitive field %s.x.",
			testClass.getCanonicalName());
		Events tests = executeTestsForClass(testClass).testEvents();
		assertFailingWithMessage(tests, msg);
	}

	@Test
	void arrayCannotBeClosed() {
		Class<?> testClass = ArrayFieldTestCase.class;
		String msg = String.format("@AutoClose is not supported on array field %s.x.", testClass.getCanonicalName());
		Events tests = executeTestsForClass(testClass).testEvents();
		assertFailingWithMessage(tests, msg);
	}

	@Test
	void nullCannotBeClosed(@TrackLogRecords LogRecordListener listener) {
		Class<?> testClass = NullCloseableFieldTestCase.class;
		String msg = String.format("Cannot @AutoClose field %s.field because it is null.",
			testClass.getCanonicalName());
		Events tests = executeTestsForClass(testClass).testEvents();
		tests.assertStatistics(stats -> stats.succeeded(1).failed(0));
		assertThat(listener.stream(Level.WARNING)).map(LogRecord::getMessage).anyMatch(msg::equals);
	}

	@Test
	void noCloseMethod() {
		assertMissingCloseMethod(NoCloseMethodTestCase.class, "close");
	}

	@Test
	void noShutdownMethod() {
		assertMissingCloseMethod(NoShutdownMethodTestCase.class, "shutdown");
	}

	/**
	 * Tests prerequisites for the {@link AutoCloseSpy} implementation.
	 */
	@Test
	void spyPermitsOnlyASingleAction() {
		AutoCloseSpy spy = new AutoCloseSpy("preconditions");

		spy.close();

		assertThatIllegalStateException().isThrownBy(spy::run).withMessage("Already closed via close()");
		assertThatIllegalStateException().isThrownBy(spy::close).withMessage("Already closed via close()");
		assertThat(recorder).containsExactly("AutoCloseTests.preconditions.close()");
	}

	@Test
	void fieldsAreProperlyClosedWithInstancePerMethodTestClass() {
		Events tests = executeTestsForClass(InstancePerMethodTestCase.class).testEvents();
		tests.assertStatistics(stats -> stats.succeeded(2));
		assertThat(recorder).containsExactly(//
			// test1()
			"InstancePerMethodTestCase.runnable.run()", //
			"InstancePerMethodTestCase.closable.close()", //
			// test2()
			"InstancePerMethodTestCase.runnable.run()", //
			"InstancePerMethodTestCase.closable.close()", //
			// Class-level cleanup
			"InstancePerMethodTestCase.staticClosable.close()"//
		);
	}

	@Test
	void fieldsAreProperlyClosedWithInstancePerClassTestClass() {
		Events tests = executeTestsForClass(InstancePerClassTestCase.class).testEvents();
		tests.assertStatistics(stats -> stats.succeeded(2));
		assertThat(InstancePerClassTestCase.closed).isTrue();
	}

	@Test
	void fieldsAreProperlyClosedWithNestedTestClassesWithInstancePerMethod() {
		Events tests = executeTestsForClass(InstancePerMethodEnclosingTestCase.NestedTestCase.class).testEvents();
		tests.assertStatistics(stats -> stats.succeeded(1));
		assertThat(recorder).containsExactly(//
			"NestedTestCase.nestedClosable.close()", //
			"InstancePerMethodEnclosingTestCase.enclosingClosable.close()", //
			"NestedTestCase.nestedStaticClosable.close()", //
			"InstancePerMethodEnclosingTestCase.enclosingStaticClosable.close()"//
		);

		// Reset tracking
		resetTracking();

		tests = executeTestsForClass(InstancePerMethodEnclosingTestCase.class).testEvents();
		tests.assertStatistics(stats -> stats.succeeded(2));
		assertThat(recorder).containsExactly(//
			"InstancePerMethodEnclosingTestCase.enclosingClosable.close()", //
			"NestedTestCase.nestedClosable.close()", //
			"InstancePerMethodEnclosingTestCase.enclosingClosable.close()", //
			"NestedTestCase.nestedStaticClosable.close()", //
			"InstancePerMethodEnclosingTestCase.enclosingStaticClosable.close()" //
		);
	}

	@Test
	void fieldsAreProperlyClosedWithNestedTestClassesWithInstancePerClass() {
		// With test instance lifecycle "per class" mode, we actually expect the
		// same behavior for the closing of all fields when the nested test class
		// is run standalone AND when it's run along with its enclosing class.
		String[] expected = { //
				"NestedTestCase.nestedStaticClosable.close()", //
				"NestedTestCase.nestedClosable.close()", //
				"InstancePerClassEnclosingTestCase.enclosingStaticClosable.close()", //
				"InstancePerClassEnclosingTestCase.enclosingClosable.close()" //
		};

		Events tests = executeTestsForClass(InstancePerClassEnclosingTestCase.NestedTestCase.class).testEvents();
		tests.assertStatistics(stats -> stats.succeeded(1));
		assertThat(recorder).containsExactly(expected);

		// Reset tracking
		resetTracking();

		tests = executeTestsForClass(InstancePerClassEnclosingTestCase.class).testEvents();
		tests.assertStatistics(stats -> stats.succeeded(2));
		assertThat(recorder).containsExactly(expected);
	}

	@Test
	void fieldsAreProperlyClosedWithinTestClassHierarchy() {
		Events tests = executeTestsForClass(SuperTestCase.class).testEvents();
		tests.assertStatistics(stats -> stats.succeeded(1));
		assertThat(recorder).containsExactly(//
			// superTest()
			"SuperTestCase.superClosable.close()", //
			// Class-level cleanup
			"SuperTestCase.superStaticClosable.close()" //
		);

		// Reset tracking
		resetTracking();

		tests = executeTestsForClass(SubTestCase.class).testEvents();
		tests.assertStatistics(stats -> stats.succeeded(2));
		assertThat(recorder).containsExactly(//
			// superTest()
			"SubTestCase.subClosable.close()", //
			"SuperTestCase.superClosable.close()", //
			// subTest()
			"SubTestCase.subClosable.close()", //
			"SuperTestCase.superClosable.close()", //
			// Class-level cleanup in subclass
			"SubTestCase.subStaticClosable.close()", //
			// Class-level cleanup in superclass
			"SuperTestCase.superStaticClosable.close()" //
		);
	}

	private static void assertFailingWithMessage(Events testEvents, String msg) {
		testEvents//
				.assertStatistics(stats -> stats.failed(1))//
				.assertThatEvents().haveExactly(1, finishedWithFailure(message(msg)));
	}

	private void assertMissingCloseMethod(Class<?> testClass, String methodName) {
		String msg = String.format("Cannot @AutoClose field %s.field because %s does not define method %s().",
			testClass.getCanonicalName(), String.class.getName(), methodName);
		Events tests = executeTestsForClass(testClass).testEvents();
		assertFailingWithMessage(tests, msg);
	}

	interface TestInterface {

		@Test
		default void test() {
		}
	}

	static class BlankCloseMethodNameTestCase implements TestInterface {

		@AutoClose("")
		final String field = "blank";
	}

	static class PrimitiveFieldTestCase implements TestInterface {

		@AutoClose
		final int x = 0;
	}

	static class ArrayFieldTestCase implements TestInterface {

		@AutoClose
		final int[] x = {};
	}

	static class NullCloseableFieldTestCase implements TestInterface {

		@AutoClose
		final AutoCloseable field = null;
	}

	static class NoCloseMethodTestCase implements TestInterface {

		@AutoClose
		private final String field = "";
	}

	@Retention(RetentionPolicy.RUNTIME)
	@AutoClose("shutdown")
	@interface AutoShutdown {
	}

	static class NoShutdownMethodTestCase implements TestInterface {

		@AutoShutdown
		private final String field = "";
	}

	@TestInstance(PER_METHOD)
	static class InstancePerMethodTestCase {

		@AutoClose
		private static AutoCloseable staticClosable;

		@AutoClose
		private static final AutoCloseable nullStatic = null;

		@AutoClose
		private final AutoCloseable closable = new AutoCloseSpy("closable");

		@AutoClose("   run      ") // intentionally contains extra whitespace.
		private final Runnable runnable = new AutoCloseSpy("runnable");

		@AutoClose
		private final AutoCloseable nullField = null;

		@BeforeAll
		static void setup() {
			staticClosable = new AutoCloseSpy("staticClosable");
		}

		@Test
		void test1() {
		}

		@Test
		void test2() {
		}
	}

	@TestInstance(PER_CLASS)
	static class InstancePerClassTestCase {

		static boolean closed = false;

		@AutoClose
		final AutoCloseable field = () -> closed = true;

		@Test
		void test1() {
			assertThat(closed).isFalse();
		}

		@Test
		void test2() {
			assertThat(closed).isFalse();
		}
	}

	@TestInstance(PER_METHOD)
	static class InstancePerMethodEnclosingTestCase implements TestInterface {

		@AutoClose
		static AutoCloseSpy enclosingStaticClosable;

		@AutoClose
		final AutoCloseable enclosingClosable = new AutoCloseSpy("enclosingClosable");

		@BeforeAll
		static void setup() {
			enclosingStaticClosable = new AutoCloseSpy("enclosingStaticClosable");
		}

		@Nested
		@TestInstance(PER_METHOD)
		class NestedTestCase implements TestInterface {

			@AutoClose
			static AutoCloseSpy nestedStaticClosable;

			@AutoClose
			final AutoCloseable nestedClosable = new AutoCloseSpy("nestedClosable");

			@BeforeAll
			static void setup() {
				nestedStaticClosable = new AutoCloseSpy("nestedStaticClosable");
			}
		}
	}

	@TestInstance(PER_CLASS)
	static class InstancePerClassEnclosingTestCase implements TestInterface {

		@AutoClose
		static AutoCloseSpy enclosingStaticClosable;

		@AutoClose
		final AutoCloseable enclosingClosable = new AutoCloseSpy("enclosingClosable");

		@BeforeAll
		static void setup() {
			enclosingStaticClosable = new AutoCloseSpy("enclosingStaticClosable");
		}

		@Nested
		@TestInstance(PER_CLASS)
		class NestedTestCase implements TestInterface {

			@AutoClose
			static AutoCloseSpy nestedStaticClosable;

			@AutoClose
			final AutoCloseable nestedClosable = new AutoCloseSpy("nestedClosable");

			@BeforeAll
			static void setup() {
				nestedStaticClosable = new AutoCloseSpy("nestedStaticClosable");
			}
		}
	}

	static class SuperTestCase {

		@AutoClose
		static AutoCloseable superStaticClosable;

		@AutoClose
		final AutoCloseable superClosable = new AutoCloseSpy("superClosable");

		@BeforeAll
		// WARNING: if this method is named setup() AND the @BeforeAll method in
		// SubTestCase is also named setup(), the latter will "hide" the former.
		static void superSetup() {
			superStaticClosable = new AutoCloseSpy("superStaticClosable");
		}

		@Test
		void superTest() {
		}
	}

	static class SubTestCase extends SuperTestCase {

		@AutoClose
		static AutoCloseable subStaticClosable;

		@AutoClose
		final AutoCloseable subClosable = new AutoCloseSpy("subClosable");

		@BeforeAll
		static void subSetup() {
			subStaticClosable = new AutoCloseSpy("subStaticClosable");
		}

		@Test
		void subTest() {
		}
	}

	static class AutoCloseSpy implements AutoCloseable, Runnable {

		private final String prefix;
		private String invokedMethod = null;

		AutoCloseSpy(String prefix) {
			Class<?> callerClass = StackWalker.getInstance(RETAIN_CLASS_REFERENCE).getCallerClass();
			this.prefix = callerClass.getSimpleName() + "." + prefix + ".";
		}

		@Override
		public void run() {
			recordInvocation("run()");
		}

		@Override
		public void close() {
			recordInvocation("close()");
		}

		private void recordInvocation(String methodName) {
			if (this.invokedMethod != null) {
				throw new IllegalStateException("Already closed via " + this.invokedMethod);
			}
			this.invokedMethod = methodName;
			recorder.add(this.prefix + this.invokedMethod);
		}
	}

}
