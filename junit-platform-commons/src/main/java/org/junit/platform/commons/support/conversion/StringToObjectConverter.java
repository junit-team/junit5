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

import org.jspecify.annotations.Nullable;

/**
 * Internal API for converting arguments of type {@link String} to a specified
 * target type.
 */
abstract class StringToObjectConverter implements Converter {

	@Override
	public final boolean canConvert(@Nullable Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
		return canConvert(targetType.getType());
	}

	/**
	 * Determine if this converter can convert from a {@link String} to the
	 * supplied target type (which is guaranteed to be a wrapper type for
	 * primitives &mdash; for example, {@link Integer} instead of {@code int}).
	 */
	abstract boolean canConvert(@Nullable Class<?> targetType);

	@Override
	public final Object convert(@Nullable Object source, TypeDescriptor sourceType, TypeDescriptor targetType,
			ClassLoader classLoader) {
		return convert((String) source, targetType.getType(), classLoader);
	}

	/**
	 * Convert the supplied {@link String} to the supplied target type (which is
	 * guaranteed to be a wrapper type for primitives &mdash; for example,
	 * {@link Integer} instead of {@code int}).
	 *
	 * <p>This method will only be invoked if {@link #canConvert(Class)}
	 * returned {@code true} for the same target type.
	 */
	abstract Object convert(@Nullable String source, @Nullable Class<?> targetType, ClassLoader classLoader);

}
