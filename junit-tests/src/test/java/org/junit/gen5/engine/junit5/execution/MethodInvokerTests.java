/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.execution;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.gen5.api.Assertions.assertSame;
import static org.junit.gen5.api.Assertions.expectThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.function.Function;

import org.junit.gen5.api.Test;
import org.junit.gen5.api.extension.ExtensionContext;
import org.junit.gen5.api.extension.ParameterResolutionException;
import org.junit.gen5.api.extension.ParameterResolver;
import org.junit.gen5.engine.junit5.extension.ExtensionRegistry;

/**
 * Unit tests for {@link MethodInvoker}.
 *
 * @since 5.0
 */
// @FullLogging(MethodInvoker.class)
class MethodInvokerTests {

	private final MethodSource instance = mock(MethodSource.class);
	private MethodInvocationContext methodInvocationContext;
	private ExtensionRegistry extensionRegistry = ExtensionRegistry.createEmptyRegistry();
	private ExtensionContext extensionContext = null;

	@Test
	void invokingMethodsWithoutParameterDoesNotDependOnExtensions() throws Exception {
		testMethodWithNoParameters();
		extensionRegistry = null;

		invokeMethod();

		verify(instance).noParameter();
	}

	@Test
	void resolveArgumentsViaParameterResolver() {
		testMethodWithASingleStringParameter();
		thereIsAParameterResolverThatResolvesTheParameterTo("argument");

		invokeMethod();

		verify(instance).singleStringParameter("argument");
	}

	@Test
	void resolveMultipleArguments() {
		testMethodWith("multipleParameters", String.class, String.class, String.class);
		register(ConfigurableParameterResolver.supportsAndResolvesTo(Parameter::getName));

		invokeMethod();

		verify(instance).multipleParameters("arg0", "arg1", "arg2");
	}

	@Test
	void onlyConsiderParameterResolversThatSupportAParticularParameter() {
		testMethodWithASingleStringParameter();
		thereIsAParameterResolverThatDoesNotSupportThisParameter();
		thereIsAParameterResolverThatResolvesTheParameterTo("something");

		invokeMethod();

		verify(instance).singleStringParameter("something");
	}

	@Test
	void passContextInformationToParameterResolverMethods() {
		anyTestMethodWithAtLeasOneParameter();
		this.extensionContext = mock(ExtensionContext.class);
		ArgumentRecordingParameterResolver extension = new ArgumentRecordingParameterResolver();
		register(extension);

		invokeMethod();

		assertSame(extensionContext, extension.supportsArguments.extensionContext);
		assertSame(instance, extension.supportsArguments.target.get());
		assertSame(extensionContext, extension.resolveArguments.extensionContext);
		assertSame(instance, extension.resolveArguments.target.get());
	}

	@Test
	void invocationOfMethodsWithPrimitiveTypesIsSupported() {
		testMethodWithASinglePrimitiveIntParameter();
		thereIsAParameterResolverThatResolvesTheParameterTo(42);

		invokeMethod();

		verify(instance).primitiveParameterInt(42);
	}

	@Test
	void nullIsAViableArgumentIfAReferenceTypeParameterIsExpected() {
		testMethodWithASingleStringParameter();
		thereIsAParameterResolverThatResolvesTheParameterTo(null);

		invokeMethod();

		verify(instance).singleStringParameter(null);
	}

	@Test
	void reportThatNullIsNotAViableArgumentIfAPrimitiveTypeIsExpected() {
		testMethodWithASinglePrimitiveIntParameter();
		thereIsAParameterResolverThatResolvesTheParameterTo(null);

		ParameterResolutionException caught = expectThrows(ParameterResolutionException.class, this::invokeMethod);

		// @formatter:off
		assertThat(caught.getMessage())
				.contains("resolved a null value for parameter [int arg0]")
				.contains("but a primitive of type [int] is required.");
		// @formatter:on
	}

	@Test
	void reportIfThereIsNoParameterResolverThatSupportsTheParameter() {
		testMethodWithASingleStringParameter();

		ParameterResolutionException caught = expectThrows(ParameterResolutionException.class, this::invokeMethod);

		assertThat(caught.getMessage()).contains("parameter [java.lang.String arg0]");
	}

	@Test
	void reportIfThereAreMultipleParameterResolversThatSupportTheParameter() {
		testMethodWithASingleStringParameter();
		thereIsAParameterResolverThatResolvesTheParameterTo("one");
		thereIsAParameterResolverThatResolvesTheParameterTo("two");

		ParameterResolutionException caught = expectThrows(ParameterResolutionException.class, this::invokeMethod);

		// @formatter:off
		assertThat(caught.getMessage())
				.contains("parameter [java.lang.String arg0]")
				.contains(ConfigurableParameterResolver.class.getName() + ", " + ConfigurableParameterResolver.class.getName());
		// @formatter:on
	}

	@Test
	void reportTypeMismatchBetweenParameterAndResolvedParameter() {
		testMethodWithASingleStringParameter();
		thereIsAParameterResolverThatResolvesTheParameterTo(BigDecimal.ONE);

		ParameterResolutionException caught = expectThrows(ParameterResolutionException.class, this::invokeMethod);

		// @formatter:off
		assertThat(caught.getMessage())
				.contains("resolved a value of type [java.math.BigDecimal] for parameter [java.lang.String arg0]")
				.contains("but a value assignment compatible with [java.lang.String] is required.");
		// @formatter:on
	}

	@Test
	void wrapAllExceptionsThrownDuringParameterResolutionIntoAParameterResolutionException() {
		anyTestMethodWithAtLeasOneParameter();
		IllegalArgumentException cause = anyExceptionButParameterResolutionException();
		throwDuringParameterResolution(cause);

		ParameterResolutionException caught = expectThrows(ParameterResolutionException.class, this::invokeMethod);

		assertSame(cause, caught.getCause(), () -> "cause should be present");
		assertThat(caught.getMessage()).startsWith("Failed to resolve parameter [java.lang.String arg0]");
	}

	@Test
	void doNotWrapThrownExceptionIfItIsAlreadyAParameterResolutionException() {
		anyTestMethodWithAtLeasOneParameter();
		ParameterResolutionException cause = new ParameterResolutionException("custom message");
		throwDuringParameterResolution(cause);

		ParameterResolutionException caught = expectThrows(ParameterResolutionException.class, this::invokeMethod);

		assertSame(cause, caught);
	}

	private IllegalArgumentException anyExceptionButParameterResolutionException() {
		return new IllegalArgumentException();
	}

	private void throwDuringParameterResolution(RuntimeException parameterResolutionException) {
		register(ConfigurableParameterResolver.onAnyCallThrow(parameterResolutionException));
	}

	private void thereIsAParameterResolverThatResolvesTheParameterTo(Object argument) {
		register(ConfigurableParameterResolver.supportsAndResolvesTo(parameter -> argument));
	}

	private void thereIsAParameterResolverThatDoesNotSupportThisParameter() {
		register(ConfigurableParameterResolver.withoutSupport());
	}

	private void register(ParameterResolver extension) {
		extensionRegistry.registerExtension(extension, this);
	}

	private void anyTestMethodWithAtLeasOneParameter() {
		testMethodWithASingleStringParameter();
	}

	private void testMethodWithNoParameters() {
		testMethodWith("noParameter");
	}

	private void testMethodWithASingleStringParameter() {
		testMethodWith("singleStringParameter", String.class);
	}

	private void testMethodWithASinglePrimitiveIntParameter() {
		testMethodWith("primitiveParameterInt", int.class);
	}

	private void invokeMethod() {
		new MethodInvoker(extensionContext, extensionRegistry).invoke(methodInvocationContext);
	}

	private void testMethodWith(final String methodName, Class<?>... parameterTypes) {
		methodInvocationContext = new MethodInvocationContext() {

			@Override
			public Object getInstance() {
				return instance;
			}

			@Override
			public Method getMethod() {
				try {
					return instance.getClass().getDeclaredMethod(methodName, parameterTypes);
				}
				catch (NoSuchMethodException e) {
					throw new RuntimeException(e);
				}
			}
		};
	}

	// -------------------------------------------------------------------------

	static class ArgumentRecordingParameterResolver implements ParameterResolver {

		Arguments supportsArguments;
		Arguments resolveArguments;

		static class Arguments {

			final Optional<Object> target;
			final ExtensionContext extensionContext;

			Arguments(Optional<Object> target, ExtensionContext extensionContext) {
				this.target = target;
				this.extensionContext = extensionContext;
			}
		}

		@Override
		public boolean supports(Parameter parameter, Optional<Object> target, ExtensionContext extensionContext) {
			supportsArguments = new Arguments(target, extensionContext);
			return true;
		}

		@Override
		public Object resolve(Parameter parameter, Optional<Object> target, ExtensionContext extensionContext) {
			resolveArguments = new Arguments(target, extensionContext);
			return null;
		}
	}

	static class ConfigurableParameterResolver implements ParameterResolver {

		static ParameterResolver onAnyCallThrow(RuntimeException runtimeException) {
			return new ConfigurableParameterResolver(parameter -> {
				throw runtimeException;
			}, parameter -> {
				throw runtimeException;
			});
		}

		static ParameterResolver supportsAndResolvesTo(Function<Parameter, Object> resolve) {
			return new ConfigurableParameterResolver(parameter -> true, resolve);
		}

		static ParameterResolver withoutSupport() {
			return new ConfigurableParameterResolver(parameter -> false, parameter -> {
				throw new UnsupportedOperationException();
			});
		}

		private final Function<Parameter, Boolean> supports;
		private final Function<Parameter, Object> resolve;

		private ConfigurableParameterResolver(Function<Parameter, Boolean> supports,
				Function<Parameter, Object> resolve) {
			this.supports = supports;
			this.resolve = resolve;
		}

		@Override
		public boolean supports(Parameter parameter, Optional<Object> target, ExtensionContext extensionContext)
				throws ParameterResolutionException {
			return supports.apply(parameter);
		}

		@Override
		public Object resolve(Parameter parameter, Optional<Object> target, ExtensionContext extensionContext)
				throws ParameterResolutionException {
			return resolve.apply(parameter);
		}
	}

	interface MethodSource {

		void noParameter();

		void singleStringParameter(String parameter);

		void primitiveParameterInt(int parameter);

		void multipleParameters(String first, String second, String third);
	}

}
