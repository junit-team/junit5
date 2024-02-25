/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.commons.support.conversion;

import org.junit.platform.commons.util.ReflectionUtils;

class StringToClassConverter implements StringToObjectConverter {

	@Override
	public boolean canConvertTo(Class<?> targetType) {
		return targetType == Class.class;
	}

	@Override
	public Object convert(String source, Class<?> targetType) throws Exception {
		throw new UnsupportedOperationException("Invoke convert(String, Class<?>, ClassLoader) instead");
	}

	@Override
	public Object convert(String className, Class<?> targetType, ClassLoader classLoader) throws Exception {
		// @formatter:off
		return ReflectionUtils.tryToLoadClass(className, classLoader)
				.getOrThrow(cause -> new ConversionException(
						"Failed to convert String \"" + className + "\" to type java.lang.Class", cause));
		// @formatter:on
	}

}
