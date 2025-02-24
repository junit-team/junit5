/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params;

import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.junit.jupiter.params.ParameterizedContainerClassContext.InjectionType.CONSTRUCTOR;
import static org.junit.platform.commons.support.AnnotationSupport.findAnnotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ContainerTemplateInvocationContext;
import org.junit.jupiter.api.extension.ContainerTemplateInvocationContextProvider;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.PreconditionViolationException;

/**
 * @since 5.13
 */
class ParameterizedContainerExtension extends ParameterizedInvocationContextProvider<ContainerTemplateInvocationContext>
		implements ContainerTemplateInvocationContextProvider, ParameterResolver {

	private static final String DECLARATION_CONTEXT_KEY = "context";

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {

		// This method always returns `false` because it is not intended to be used as a parameter resolver.
		// Instead, it is used to provide a better error message when `TestInstance.Lifecycle.PER_CLASS` is
		// attempted to be combined with constructor injection of parameters.

		if (isDeclaredOnTestClassConstructor(parameterContext, extensionContext)) {
			validateAndStoreClassContext(extensionContext);
		}

		return false;
	}

	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {

		// Should never be called (see comment above).

		throw new JUnitException("Unexpected call to resolveParameter");
	}

	@Override
	public boolean supportsContainerTemplate(ExtensionContext extensionContext) {
		return validateAndStoreClassContext(extensionContext);
	}

	@Override
	public Stream<? extends ContainerTemplateInvocationContext> provideContainerTemplateInvocationContexts(
			ExtensionContext extensionContext) {

		return provideInvocationContexts(extensionContext, getDeclarationContext(extensionContext));
	}

	@Override
	public boolean mayReturnZeroContainerTemplateInvocationContexts(ExtensionContext extensionContext) {
		return getDeclarationContext(extensionContext).isAllowingZeroInvocations();
	}

	private static boolean isDeclaredOnTestClassConstructor(ParameterContext parameterContext,
			ExtensionContext extensionContext) {

		Executable declaringExecutable = parameterContext.getDeclaringExecutable();
		return declaringExecutable instanceof Constructor //
				&& declaringExecutable.getDeclaringClass().equals(extensionContext.getTestClass().orElse(null));
	}

	private boolean validateAndStoreClassContext(ExtensionContext extensionContext) {

		Store store = getStore(extensionContext);
		if (store.get(DECLARATION_CONTEXT_KEY) != null) {
			return true;
		}

		Optional<ParameterizedContainer> annotation = findAnnotation(extensionContext.getTestClass(),
			ParameterizedContainer.class);
		if (!annotation.isPresent()) {
			return false;
		}

		store.put(DECLARATION_CONTEXT_KEY,
			createClassContext(extensionContext, extensionContext.getRequiredTestClass(), annotation.get()));

		return true;
	}

	private static ParameterizedContainerClassContext createClassContext(ExtensionContext extensionContext,
			Class<?> testClass, ParameterizedContainer annotation) {

		TestInstance.Lifecycle lifecycle = extensionContext.getTestInstanceLifecycle() //
				.orElseThrow(() -> new PreconditionViolationException("TestInstance.Lifecycle not present"));

		ParameterizedContainerClassContext classContext = new ParameterizedContainerClassContext(testClass, annotation,
			lifecycle);

		if (lifecycle == PER_CLASS && classContext.getInjectionType() == CONSTRUCTOR) {
			throw new PreconditionViolationException(
				"Constructor injection is not supported for @ParameterizedContainer classes with @TestInstance(Lifecycle.PER_CLASS)");
		}

		return classContext;
	}

	private ParameterizedContainerClassContext getDeclarationContext(ExtensionContext extensionContext) {
		return getStore(extensionContext)//
				.get(DECLARATION_CONTEXT_KEY, ParameterizedContainerClassContext.class);
	}

	private Store getStore(ExtensionContext context) {
		return context.getStore(
			Namespace.create(ParameterizedContainerExtension.class, context.getRequiredTestClass()));
	}

}
