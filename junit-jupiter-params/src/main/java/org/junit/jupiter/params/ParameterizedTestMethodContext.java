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

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;

import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.platform.commons.util.Preconditions;

/**
 * Encapsulates access to the parameters of a parameterized test method and
 * caches the converters and aggregators used to resolve them.
 *
 * @since 5.3
 */
class ParameterizedTestMethodContext implements ParameterizedDeclarationContext<TestTemplateInvocationContext> {

	private final Method method;
	private final ParameterizedTest annotation;
	private final ResolverFacade resolverFacade;

	ParameterizedTestMethodContext(Method method, ParameterizedTest annotation) {
		this.method = Preconditions.notNull(method, "method must not be null");
		this.annotation = Preconditions.notNull(annotation, "annotation must not be null");
		this.resolverFacade = ResolverFacade.create(method);
	}

	@Override
	public ParameterizedTest getAnnotation() {
		return this.annotation;
	}

	@Override
	public AnnotatedElement getAnnotatedElement() {
		return this.method;
	}

	@Override
	public String getDisplayNamePattern() {
		return this.annotation.name();
	}

	@Override
	public boolean isAutoClosingArguments() {
		return this.annotation.autoCloseArguments();
	}

	@Override
	public boolean isAllowingZeroInvocations() {
		return this.annotation.allowZeroInvocations();
	}

	@Override
	public ArgumentCountValidationMode getArgumentCountValidationMode() {
		return this.annotation.argumentCountValidation();
	}

	/**
	 * Determine if the {@link Method} represented by this context has a
	 * <em>potentially</em> valid signature (i.e., formal parameter
	 * declarations) with regard to aggregators.
	 *
	 * <p>This method takes a best-effort approach at enforcing the following
	 * policy for parameterized test methods that accept aggregators as arguments.
	 *
	 * <ol>
	 * <li>zero or more <em>indexed arguments</em> come first.</li>
	 * <li>zero or more <em>aggregators</em> come next.</li>
	 * <li>zero or more arguments supplied by other {@code ParameterResolver}
	 * implementations come last.</li>
	 * </ol>
	 *
	 * @return {@code true} if the method has a potentially valid signature
	 */
	boolean hasPotentiallyValidSignature() {
		int indexOfPreviousAggregator = -1;
		for (int i = 0; i < getResolverFacade().getParameterCount(); i++) {
			if (getResolverFacade().isAggregator(i)) {
				if ((indexOfPreviousAggregator != -1) && (i != indexOfPreviousAggregator + 1)) {
					return false;
				}
				indexOfPreviousAggregator = i;
			}
		}
		return true;
	}

	@Override
	public ResolverFacade getResolverFacade() {
		return this.resolverFacade;
	}

	@Override
	public TestTemplateInvocationContext createInvocationContext(ParameterizedInvocationNameFormatter formatter,
			Arguments arguments, int invocationIndex) {
		return new ParameterizedTestInvocationContext(this, formatter, arguments, invocationIndex);
	}

}
