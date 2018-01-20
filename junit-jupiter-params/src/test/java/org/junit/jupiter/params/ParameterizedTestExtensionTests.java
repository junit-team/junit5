/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.ParameterizedTestExtension.arguments;

import java.io.FileNotFoundException;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.platform.commons.JUnitException;

/**
 * Unit tests for {@link ParameterizedTestExtension}.
 *
 * @since 5.0
 */
class ParameterizedTestExtensionTests {

	private final ParameterizedTestExtension parameterizedTestExtension = new ParameterizedTestExtension();

	static boolean streamWasClosed = false;

	@Test
	void supportsReturnsFalseForMissingTestMethod() {
		ExtensionContext extensionContextWithoutTestMethod = getExtensionContextReturningSingleMethod(
			new TestCaseWithoutMethod());
		assertFalse(this.parameterizedTestExtension.supportsTestTemplate(extensionContextWithoutTestMethod));
	}

	@Test
	void supportsReturnsFalseForTestMethodWithoutParameterizedTestAnnotation() {
		ExtensionContext extensionContextWithUnAnnotatedTestMethod = getExtensionContextReturningSingleMethod(
			new TestCaseWithMethod());
		assertFalse(this.parameterizedTestExtension.supportsTestTemplate(extensionContextWithUnAnnotatedTestMethod));
	}

	@Test
	void supportsReturnsTrueForTestMethodWithParameterizedTestAnnotation() {
		ExtensionContext extensionContextWithAnnotatedTestMethod = getExtensionContextReturningSingleMethod(
			new TestCaseWithAnnotatedMethod());
		assertTrue(this.parameterizedTestExtension.supportsTestTemplate(extensionContextWithAnnotatedTestMethod));
	}

	@Test
	void streamsReturnedByProvidersAreClosedWhenCallingProvide() {
		ExtensionContext extensionContext = getExtensionContextReturningSingleMethod(
			new TestCaseWithArgumentSourceAnnotatedMethod());

		Stream<TestTemplateInvocationContext> stream = this.parameterizedTestExtension.provideTestTemplateInvocationContexts(
			extensionContext);

		//cause the stream to be evaluated
		stream.count();
		assertTrue(streamWasClosed);
	}

	@Test
	void argumentsRethrowsOriginalExceptionFromProviderAsUncheckedException() {
		ArgumentsProvider failingProvider = (context) -> {
			throw new FileNotFoundException("a message");
		};

		FileNotFoundException exception = assertThrows(FileNotFoundException.class,
			() -> arguments(failingProvider, null));
		assertEquals("a message", exception.getMessage());
	}

	@Test
	void throwsExceptionWhenParameterizedTestIsNotInvokedAtLeastOnce() {
		ExtensionContext extensionContextWithAnnotatedTestMethod = getExtensionContextReturningSingleMethod(
			new TestCaseWithAnnotatedMethod());

		Stream<TestTemplateInvocationContext> stream = this.parameterizedTestExtension.provideTestTemplateInvocationContexts(
			extensionContextWithAnnotatedTestMethod);
		//cause the stream to be evaluated
		stream.toArray();
		JUnitException exception = assertThrows(JUnitException.class, stream::close);

		assertThat(exception).hasMessage(
			"Configuration error: You must provide at least one argument for this @ParameterizedTest");
	}

	private ExtensionContext getExtensionContextReturningSingleMethod(Object testCase) {

		// @formatter:off
		Optional<Method> optional = Arrays.stream(testCase.getClass().getDeclaredMethods())
				.filter(method -> method.getName().equals("method"))
				.findFirst();
		// @formatter:on

		return new ExtensionContext() {

			@Override
			public Optional<Method> getTestMethod() {
				return optional;
			}

			@Override
			public Optional<ExtensionContext> getParent() {
				return null;
			}

			@Override
			public ExtensionContext getRoot() {
				return this;
			}

			@Override
			public String getUniqueId() {
				return null;
			}

			@Override
			public String getDisplayName() {
				return null;
			}

			@Override
			public Set<String> getTags() {
				return null;
			}

			@Override
			public Optional<AnnotatedElement> getElement() {
				return null;
			}

			@Override
			public Optional<Class<?>> getTestClass() {
				return null;
			}

			@Override
			public Optional<Lifecycle> getTestInstanceLifecycle() {
				return Optional.empty();
			}

			@Override
			public java.util.Optional<Object> getTestInstance() {
				return Optional.empty();
			}

			@Override
			public Optional<Throwable> getExecutionException() {
				return Optional.empty();
			}

			@Override
			public Optional<String> getConfigurationParameter(String key) {
				return Optional.empty();
			}

			@Override
			public void publishReportEntry(Map<String, String> map) {
			}

			@Override
			public Store getStore(Namespace namespace) {
				return null;
			}
		};
	}

	static class TestCaseWithoutMethod {
	}

	static class TestCaseWithMethod {

		void method() {
		}
	}

	static class TestCaseWithAnnotatedMethod {

		@ParameterizedTest
		void method() {
		}
	}

	static class TestCaseWithArgumentSourceAnnotatedMethod {

		@ParameterizedTest
		@ArgumentsSource(ArgumentsProviderWithCloseHandler.class)
		void method(String parameter) {
		}

	}

	static class ArgumentsProviderWithCloseHandler implements ArgumentsProvider {

		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
			Stream<Arguments> argumentsStream = Stream.of("foo", "bar").map(Arguments::of);
			return argumentsStream.onClose(() -> streamWasClosed = true);
		}
	}

}
