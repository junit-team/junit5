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
import org.junit.gen5.api.extension.ExtendWith;
import org.junit.gen5.api.extension.ExtensionContext;
import org.junit.gen5.api.extension.MethodInvocationContext;
import org.junit.gen5.api.extension.MethodParameterResolver;
import org.junit.gen5.api.extension.ParameterResolutionException;
import org.junit.gen5.engine.junit5.extension.ExtensionRegistry;

/**
 * Microtests for {@link MethodInvoker}
 */
@ExtendWith({ FullLogging.class })
@FullLogging.LoggerSelector({ MethodInvoker.class })
class MethodInvokerTests {

	private final MethodSource instance = mock(MethodSource.class);
	private MethodInvocationContext methodInvocationContext;
	private ExtensionRegistry extensionRegistry = new ExtensionRegistry(Optional.empty());
	private ExtensionContext extensionContext = null;

	@Test
	void invokingMethodsWithoutParameterDoesNotDependOnExtensions() throws Exception {
		testMethodWithNoParameters();
		extensionRegistry = null;

		invokeMethod();

		verify(instance).noParameter();
	}

	@Test
	void resolveArgumentsViaMethodParameterResolver() {
		testMethodWithASingleStringParameter();
		thereIsAParameterResolverThatResolvesTheParameterTo("argument");

		invokeMethod();

		verify(instance).singleStringParameter("argument");
	}

	@Test
	void resolveMultipleArguments() {
		testMethodWith("multipleParameters", String.class, String.class, String.class);
		register(ConfigurableMethodParameterResolver.supportsAndResolvesTo(Parameter::getName));

		invokeMethod();

		verify(instance).multipleParameters("arg0", "arg1", "arg2");
	}

	@Test
	void onlyConsiderMethodParameterResolversThatSupportAParticularParameter() {
		testMethodWithASingleStringParameter();
		thereIsAParameterResolverThatDoesNotSupportThisParameter();
		thereIsAParameterResolverThatResolvesTheParameterTo("something");

		invokeMethod();

		verify(instance).singleStringParameter("something");
	}

	@Test
	void passContextInformatinoToMethodParameterResolverMethods() {
		anyTestMethodWithAtLeasOneParameter();
		this.extensionContext = mock(ExtensionContext.class);
		ArgumentRecordingMethodParameterResolver extension = new ArgumentRecordingMethodParameterResolver();
		register(extension);

		invokeMethod();

		assertSame(extensionContext, extension.supportsArguments.extensionContext);
		assertSame(methodInvocationContext, extension.supportsArguments.methodInvocationContext);
		assertSame(extensionContext, extension.resolveArguments.extensionContext);
		assertSame(methodInvocationContext, extension.resolveArguments.methodInvocationContext);
	}

	@Test
	void invokeMethodsWithPrimitiveTypesIsSupported() {
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
	void reportIfThereIsNoMethodParameterResolverThatSupportsTheParameter() {
		testMethodWithASingleStringParameter();

		ParameterResolutionException caught = expectThrows(ParameterResolutionException.class, this::invokeMethod);

		assertThat(caught.getMessage()).contains("parameter [java.lang.String arg0]");
	}

	@Test
	void reportIfThereAreMultipleMethodParameterResolversThatSupportTheParameter() {
		testMethodWithASingleStringParameter();
		thereIsAParameterResolverThatResolvesTheParameterTo("one");
		thereIsAParameterResolverThatResolvesTheParameterTo("two");

		ParameterResolutionException caught = expectThrows(ParameterResolutionException.class, this::invokeMethod);

		// @formatter:off
		assertThat(caught.getMessage())
				.contains("parameter [java.lang.String arg0]")
				.contains("org.junit.gen5.engine.junit5.execution.ConfigurableMethodParameterResolver, org.junit.gen5.engine.junit5.execution.ConfigurableMethodParameterResolver");
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
		ParameterResolutionException cause = new ParameterResolutionException("Stand in message");
		throwDuringParameterResolution(cause);

		ParameterResolutionException caught = expectThrows(ParameterResolutionException.class, this::invokeMethod);

		assertSame(cause, caught);
	}

	private IllegalArgumentException anyExceptionButParameterResolutionException() {
		return new IllegalArgumentException();
	}

	private void throwDuringParameterResolution(RuntimeException parameterResolutionException) {
		register(ConfigurableMethodParameterResolver.onAnyCallThrow(parameterResolutionException));
	}

	private void thereIsAParameterResolverThatResolvesTheParameterTo(Object argument) {
		register(ConfigurableMethodParameterResolver.supportsAndResolvesTo(parameter -> argument));
	}

	private void thereIsAParameterResolverThatDoesNotSupportThisParameter() {
		register(ConfigurableMethodParameterResolver.withoutSupport());
	}

	private void register(MethodParameterResolver extension) {
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

	private MethodInvocationContext testMethodWith(final String methodName, Class<?>... parameterTypes) {
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
		return methodInvocationContext;
	}
}

class ArgumentRecordingMethodParameterResolver implements MethodParameterResolver {

	public Arguments supportsArguments;
	public Arguments resolveArguments;

	public static class Arguments {

		public final MethodInvocationContext methodInvocationContext;
		public final ExtensionContext extensionContext;

		public Arguments(MethodInvocationContext methodInvocationContext, ExtensionContext extensionContext) {
			this.methodInvocationContext = methodInvocationContext;
			this.extensionContext = extensionContext;
		}
	}

	@Override
	public boolean supports(Parameter parameter, MethodInvocationContext methodInvocationContext,
			ExtensionContext extensionContext) throws ParameterResolutionException {
		supportsArguments = new Arguments(methodInvocationContext, extensionContext);
		return true;
	}

	@Override
	public Object resolve(Parameter parameter, MethodInvocationContext methodInvocationContext,
			ExtensionContext extensionContext) throws ParameterResolutionException {
		resolveArguments = new Arguments(methodInvocationContext, extensionContext);
		return null;
	}
}

class ConfigurableMethodParameterResolver implements MethodParameterResolver {

	static MethodParameterResolver onAnyCallThrow(RuntimeException runtimeException) {
		return new ConfigurableMethodParameterResolver(parameter -> {
			throw runtimeException;
		}, parameter -> {
			throw runtimeException;
		});
	}

	static MethodParameterResolver supportsAndResolvesTo(Function<Parameter, Object> resolve) {
		return new ConfigurableMethodParameterResolver(parameter -> true, resolve);
	}

	static MethodParameterResolver withoutSupport() {
		return new ConfigurableMethodParameterResolver(parameter -> false, parameter -> {
			throw new UnsupportedOperationException();
		});
	}

	private final Function<Parameter, Boolean> supports;
	private final Function<Parameter, Object> resolve;

	private ConfigurableMethodParameterResolver(Function<Parameter, Boolean> supports,
			Function<Parameter, Object> resolve) {
		this.supports = supports;
		this.resolve = resolve;
	}

	@Override
	public boolean supports(Parameter parameter, MethodInvocationContext methodInvocationContext,
			ExtensionContext extensionContext) throws ParameterResolutionException {
		return supports.apply(parameter);
	}

	@Override
	public Object resolve(Parameter parameter, MethodInvocationContext methodInvocationContext,
			ExtensionContext extensionContext) throws ParameterResolutionException {
		return resolve.apply(parameter);
	}
}

interface MethodSource {

	void noParameter();

	void singleStringParameter(String parameter);

	void primitiveParameterInt(int parameter);

	void multipleParameters(String first, String second, String third);
}
