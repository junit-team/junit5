/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.commons.support.conversion;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.Objects;

import org.apiguardian.api.API;
import org.jspecify.annotations.Nullable;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ReflectionUtils;

/**
 *
 *
 * @since 1.13
 */
@API(status = EXPERIMENTAL, since = "1.13")
public final class TypeDescriptor {

	private final @Nullable Class<?> type;

	public static TypeDescriptor forClass(Class<?> clazz) {
		return new TypeDescriptor(clazz);
	}

	public static TypeDescriptor forInstance(@Nullable Object instance) {
		return new TypeDescriptor(instance != null ? instance.getClass() : null);
	}

	public static TypeDescriptor forField(Field field) {
		Preconditions.notNull(field, "field must not be null");
		return new TypeDescriptor(field.getType());
	}

	public static TypeDescriptor forParameter(Parameter parameter) {
		Preconditions.notNull(parameter, "parameter must not be null");
		return new TypeDescriptor(parameter.getType());
	}

	private TypeDescriptor(@Nullable Class<?> type) {
		this.type = type;
	}

	public @Nullable Class<?> getType() {
		return type;
	}

	public @Nullable Class<?> getWrapperType() {
		if (type == null) { // FIXME parameter of ReflectionUtils.getWrapperType should be @Nullable
			return null;
		}
		Class<?> wrapperType = ReflectionUtils.getWrapperType(type);
		return wrapperType != null ? wrapperType : type;
	}

	public boolean isPrimitive() {
		return type != null && type.isPrimitive();
	}

	public String getTypeName() {
		return type != null ? type.getName() : "null";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		TypeDescriptor that = (TypeDescriptor) o;
		return Objects.equals(this.type, that.type);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(type);
	}

}
