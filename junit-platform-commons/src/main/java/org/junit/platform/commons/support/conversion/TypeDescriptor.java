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

import org.apiguardian.api.API;

/**
 *
 *
 * @since 1.13
 */
@API(status = EXPERIMENTAL, since = "1.13")
public final class TypeDescriptor {

	private final Class<?> type;

	public static TypeDescriptor forType(Class<?> type) {
		return new TypeDescriptor(type);
	}

	public static TypeDescriptor forField(Field field) {
		return new TypeDescriptor(field.getType());
	}

	public static TypeDescriptor forParameter(Parameter parameter) {
		return new TypeDescriptor(parameter.getType());
	}

	private TypeDescriptor(Class<?> type) {
		this.type = type;
	}

	public Class<?> getType() {
		return type;
	}

	public boolean isPrimitive() {
		return getType().isPrimitive();
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
		return this.type.equals(that.type);
	}

	@Override
	public int hashCode() {
		return this.type.hashCode();
	}

}
