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

import static org.junit.platform.commons.support.AnnotationSupport.isAnnotated;
import static org.junit.platform.commons.support.HierarchyTraversalMode.BOTTOM_UP;
import static org.junit.platform.commons.support.ReflectionSupport.findFields;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.platform.commons.support.AnnotationSupport;
import org.junit.platform.commons.support.ReflectionSupport;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ReflectionUtils;

class ParameterizedContainerClassContext implements ParameterizedDeclarationContext<ParameterizedContainer> {

	private final Class<?> clazz;
	private final ParameterizedContainer annotation;
	private final Map<Field, Integer> parameterFields;

	ParameterizedContainerClassContext(Class<?> clazz, ParameterizedContainer annotation) {
		this.clazz = clazz;
		this.annotation = annotation;
		// TODO #878 Test that composed annotations are supported
		List<Field> fields = findFields(clazz, it -> isAnnotated(it, Parameter.class), BOTTOM_UP);
		parameterFields = new HashMap<>(fields.size());
		for (Field field : fields) {
			// TODO #878 test precondition
			Preconditions.condition(!ReflectionUtils.isFinal(field), () -> "Field must not be final: " + field);
			ReflectionSupport.makeAccessible(field);
			int index = AnnotationSupport.findAnnotation(field, Parameter.class).get().value();
			parameterFields.put(field, index);
		}
	}

	Map<Field, Integer> getParameterFields() {
		return parameterFields;
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
		return Math.max(ReflectionUtils.getDeclaredConstructor(this.clazz).getParameterCount(),
			getMaxParameterIndexFromFields() + 1);
	}

	private int getMaxParameterIndexFromFields() {
		return parameterFields.values().stream().mapToInt(Integer::intValue).max().orElse(-1);
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
