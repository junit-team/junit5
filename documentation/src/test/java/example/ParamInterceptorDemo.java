/*
 * Copyright 2015-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package example;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.lang.reflect.Method;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@Execution(ExecutionMode.SAME_THREAD)
public class ParamInterceptorDemo {

	@ParameterizedTest
	@ValueSource(strings = { "foo", "bar" })
	@ArgumentsSource(MyProvider.class)
	@EmptySource
	@ExtendWith(MyInterceptor.class)
	void name(String value) {
		assertNotNull(value);
	}

	static class MyProvider implements ArgumentsProvider {

		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
			return Stream.of(arguments("custom"));
		}
	}

	static class MyInterceptor implements InvocationInterceptor {

		@Override
		public void interceptTestTemplateMethod(Invocation<Void> invocation,
				ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext)
				throws Throwable {
			Object value = extensionContext.getStore(ParameterizedTest.NAMESPACE).get(
				ParameterizedTest.ARGUMENTS_PROVIDER_KEY);
			System.out.println("value = " + value);
			System.out.println("args = " + invocationContext.getArguments());
			invocation.proceed();
		}
	}
}
