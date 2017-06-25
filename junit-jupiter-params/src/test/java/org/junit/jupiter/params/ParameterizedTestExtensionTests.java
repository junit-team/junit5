/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.params;

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
import org.junit.jupiter.api.extension.ContainerExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

class ParameterizedTestExtensionTests {

	ParameterizedTestExtension parameterizedTestExtension = new ParameterizedTestExtension();

	static boolean streamWasClosed = false;

	@Test
	void supportsReturnsFalseForMissingTestMethod() {
		ContainerExtensionContext extensionContextWithoutTestMethod = getExtensionContextReturningSingleMethod(
			new TestCaseWithoutMethod());
		assertFalse(this.parameterizedTestExtension.supportsTestTemplate(extensionContextWithoutTestMethod));
	}

	@Test
	void supportsReturnsFalseForTestMethodWithoutParameterizedTestAnnotation() {
		ContainerExtensionContext extensionContextWithUnAnnotatedTestMethod = getExtensionContextReturningSingleMethod(
			new TestCaseWithMethod());
		assertFalse(this.parameterizedTestExtension.supportsTestTemplate(extensionContextWithUnAnnotatedTestMethod));
	}

	@Test
	void supportsReturnsTrueForTestMethodWithParameterizedTestAnnotation() {
		ContainerExtensionContext extensionContextWithAnnotatedTestMethod = getExtensionContextReturningSingleMethod(
			new TestCaseWithAnnotatedMethod());
		assertTrue(this.parameterizedTestExtension.supportsTestTemplate(extensionContextWithAnnotatedTestMethod));
	}

	@Test
	void streamsReturnedByProvidersAreClosedWhenCallingProvide() {
		ContainerExtensionContext extensionContext = getExtensionContextReturningSingleMethod(
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

	private ContainerExtensionContext getExtensionContextReturningSingleMethod(Object testCase) {

		// @formatter:off
		Optional<Method> optional = Arrays.stream(testCase.getClass().getDeclaredMethods())
				.filter(method -> method.getName().equals("method"))
				.findFirst();
		// @formatter:on

		return new ContainerExtensionContext() {

			@Override
			public Optional<Method> getTestMethod() {
				return optional;
			}

			@Override
			public Optional<ExtensionContext> getParent() {
				return null;
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
			public java.util.Optional<Object> getTestInstance() {
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
		public Stream<? extends Arguments> provideArguments(ContainerExtensionContext context) {
			Stream<Arguments> argumentsStream = Stream.of("foo", "bar").map(Arguments::of);
			return argumentsStream.onClose(() -> streamWasClosed = true);
		}
	}

}
