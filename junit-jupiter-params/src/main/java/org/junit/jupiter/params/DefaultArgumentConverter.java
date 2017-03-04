/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.params;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.extension.ParameterContext;

class DefaultArgumentConverter implements ArgumentConverter {

	static final DefaultArgumentConverter INSTANCE = new DefaultArgumentConverter();

	private static final Map<Class<?>, Class<?>> WRAPPER_TYPES;
	static {
		Map<Class<?>, Class<?>> wrapperTypes = new HashMap<>();
		wrapperTypes.put(Boolean.TYPE, Boolean.class);
		wrapperTypes.put(Character.TYPE, Character.class);
		wrapperTypes.put(Byte.TYPE, Byte.class);
		wrapperTypes.put(Short.TYPE, Short.class);
		wrapperTypes.put(Integer.TYPE, Integer.class);
		wrapperTypes.put(Long.TYPE, Long.class);
		wrapperTypes.put(Float.TYPE, Float.class);
		wrapperTypes.put(Double.TYPE, Double.class);
		WRAPPER_TYPES = Collections.unmodifiableMap(wrapperTypes);
	}

	// TODO #14 use SimpleArgumentConverter

	@Override
	public Object convert(Object input, ParameterContext context) throws ArgumentConversionException {
		return convert(input, context.getParameter().getType());
	}

	Object convert(Object input, Class<?> targetClass) {
		if (targetClass.isInstance(input) || isWrapperClass(input, targetClass)) {
			return input;
		}
		if (input instanceof String && targetClass.equals(Integer.TYPE)) {
			return Integer.valueOf((String) input);
		}
		throw new ArgumentConversionException(
			"No implicit conversion to convert object to type " + targetClass.getName() + ": " + input);
	}

	private boolean isWrapperClass(Object input, Class<?> targetClass) {
		return targetClass.isPrimitive() && input.getClass() != null
				&& input.getClass().equals(WRAPPER_TYPES.get(targetClass));
	}
}
