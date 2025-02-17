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
import java.util.Optional;

import org.junit.platform.commons.util.ReflectionUtils;

class ParameterizedContainerClassContext implements ParameterizedDeclarationContext<ParameterizedContainer> {

	private final Class<?> clazz;
	private final ParameterizedContainer annotation;

	ParameterizedContainerClassContext(Class<?> clazz, ParameterizedContainer annotation) {
		this.clazz = clazz;
		this.annotation = annotation;
	}

	@Override
	public ParameterizedContainer getAnnotation() {
		return this.annotation;
	}

	@Override
	public AnnotatedElement getAnnotatedElement() {
		return this.clazz;
	}

	@Override
	public Optional<String> getParameterName(int parameterIndex) {
		return Optional.empty();
	}

	@Override
	public String getDisplayNamePattern() {
		// TODO #878 Read from annotation
		return ParameterizedInvocationNameFormatter.DEFAULT_DISPLAY_NAME;
	}

	@Override
	public boolean isAllowingZeroInvocations() {
		// TODO #878 Read from annotation
		return false;
	}

	@Override
	public ArgumentCountValidationMode getArgumentCountValidationMode() {
		// TODO #878 Read from annotation
		return ArgumentCountValidationMode.DEFAULT;
	}

	@Override
	public boolean hasAggregator() {
		// TODO #878 Determine from constructor/fields?
		return false;
	}

	@Override
	public int getParameterCount() {
		return ReflectionUtils.getDeclaredConstructor(this.clazz).getParameterCount();
	}

	@Override
	public boolean isAggregator(int parameterIndex) {
		// TODO #878 Determine from constructor/fields?
		return false;
	}

	@Override
	public int indexOfFirstAggregator() {
		// TODO #878 Determine from constructor/fields?
		return -1;
	}
}
