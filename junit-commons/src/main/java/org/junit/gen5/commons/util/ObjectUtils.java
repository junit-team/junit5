/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.commons.util;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

import java.lang.reflect.Array;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @since 5.0
 */
public final class ObjectUtils {

	private ObjectUtils() {
		/* no-op */
	}

	private static final Map<Class<?>, Class<?>> PRIMITIVE_TO_WRAPPER_MAP;

	static {
		Map<Class<?>, Class<?>> map = new HashMap<>();
		map.put(boolean.class, Boolean.class);
		map.put(char.class, Character.class);
		map.put(short.class, Short.class);
		map.put(byte.class, Byte.class);
		map.put(int.class, Integer.class);
		map.put(long.class, Long.class);
		map.put(float.class, Float.class);
		map.put(double.class, Double.class);
		PRIMITIVE_TO_WRAPPER_MAP = Collections.unmodifiableMap(map);
	}

	public static boolean isPrimitiveArray(Object obj) {
		if (obj == null) {
			return false;
		}

		Class<? extends Object> type = obj.getClass();
		return (type.isArray() && type.getComponentType().isPrimitive());
	}

	public static Object[] convertToObjectArray(Object obj) {
		if (obj == null) {
			return new Object[0];
		}

		Class<?> type = obj.getClass();
		Preconditions.condition(type.isArray(),
			() -> "The supplied object must be an array, not an instance of [" + type + "]");

		int length = Array.getLength(obj);
		Class<?> componentType = type.getComponentType();
		Object[] array = (Object[]) Array.newInstance(PRIMITIVE_TO_WRAPPER_MAP.get(componentType), length);

		for (int i = 0; i < length; i++) {
			array[i] = Array.get(obj, i);
		}
		return array;
	}

	public static String nullSafeToString(Class<?>... classes) {
		if (classes == null || classes.length == 0) {
			return "";
		}
		return stream(classes).map(Class::getName).collect(joining(", "));
	}

}
