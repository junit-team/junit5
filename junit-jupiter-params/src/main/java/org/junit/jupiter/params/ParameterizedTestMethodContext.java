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
import java.lang.reflect.Parameter;
import java.util.Optional;

import org.junit.platform.commons.util.Preconditions;

/**
 * Encapsulates access to the parameters of a parameterized test method and
 * caches the converters and aggregators used to resolve them.
 *
 * @since 5.3
 */
class ParameterizedTestMethodContext implements ParameterizedDeclarationContext<ParameterizedTest> {

	final Method method;
	final ParameterizedTest annotation;

	private final Parameter[] parameters;
	private final ResolverFacade resolverFacade;

	ParameterizedTestMethodContext(Method method, ParameterizedTest annotation) {
		this.method = Preconditions.notNull(method, "method must not be null");
		this.annotation = Preconditions.notNull(annotation, "annotation must not be null");
		this.parameters = method.getParameters();
		this.resolverFacade = ResolverFacade.create(method);
	}

	@Override
	public ParameterizedTest getAnnotation() {
		return annotation;
	}

	@Override
	public AnnotatedElement getAnnotatedElement() {
		return method;
	}

	@Override
	public String getDisplayNamePattern() {
		return annotation.name();
	}

	@Override
	public boolean isAllowingZeroInvocations() {
		return annotation.allowZeroInvocations();
	}

	@Override
	public ArgumentCountValidationMode getArgumentCountValidationMode() {
		return annotation.argumentCountValidation();
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

	/**
	 * Get the name of the {@link Parameter} with the supplied index, if
	 * it is present and declared before the aggregators.
	 *
	 * @return an {@code Optional} containing the name of the parameter
	 */
	@Override
	public Optional<String> getParameterName(int parameterIndex) {
		if (parameterIndex >= getResolverFacade().getParameterCount()) {
			return Optional.empty();
		}
		Parameter parameter = this.parameters[parameterIndex];
		if (!parameter.isNamePresent()) {
			return Optional.empty();
		}
		if (getResolverFacade().hasAggregator() && parameterIndex >= getResolverFacade().indexOfFirstAggregator()) {
			return Optional.empty();
		}
		return Optional.of(parameter.getName());
	}

	@Override
	public ResolverFacade getResolverFacade() {
		return this.resolverFacade;
	}

}
