/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.engine.TestDescriptor;

/**
 * <p>The pattern of the {@link #getTestId test ID} takes the form of
 * <code>{fully qualified class name}#{method name}({comma separated list
 * of method parameter types})</code>, where each method parameter type is
 * a fully qualified class name or a primitive type. For example,
 * {@code org.example.MyTests#test()} references the {@code test()} method
 * in the {@code org.example.MyTests} class that does not accept parameters.
 * Similarly, {@code org.example.MyTests#test(java.lang.String, java.math.BigDecimal)}
 * references the {@code test()} method in the {@code org.example.MyTests}
 * class that requires a {@code String} and {@code BigDecimal} as parameters.
 *
 * @author Sam Brannen
 * @since 5.0
 */

public class JavaTestDescriptorFactory {

	// The following pattern only supports descriptors for test methods.
	// TODO Support descriptors for test classes.
	// TODO Decide if we want to support descriptors for packages.
	private static final Pattern UID_PATTERN = Pattern.compile("^(.+):(.+)#(.+)\\((.*)\\)$");


	//Todo must be able to create any kind of JavaTestDescriptor
	public static JavaMethodTestDescriptor from(final String uid, TestDescriptor root) throws RuntimeException {
		Preconditions.notNull(uid, "TestDescriptor UID must not be null");

		Matcher matcher = UID_PATTERN.matcher(uid);
		Preconditions.condition(matcher.matches(),
			() -> String.format("Invalid format for %s UID: %s", JavaMethodTestDescriptor.class.getSimpleName(), uid));

		// TODO Validate contents of matched groups.
		String engineId = matcher.group(1);
		String className = matcher.group(2);
		String methodName = matcher.group(3);
		String methodParameters = matcher.group(4);

		Class<?> clazz = loadClass(className);

		System.out.println("DEBUG - method params: " + methodParameters);

		List<Class<?>> paramTypeList = new ArrayList<>();
		for (String type : methodParameters.split(",")) {
			type = type.trim();
			if (!type.isEmpty()) {
				paramTypeList.add(loadClass(type));
			}
		}

		Class<?>[] parameterTypes = paramTypeList.toArray(new Class<?>[paramTypeList.size()]);

		try {
			JavaClassTestDescriptor parent = new JavaClassTestDescriptor(clazz, root);
			Method method = clazz.getDeclaredMethod(methodName, parameterTypes);
			return new JavaMethodTestDescriptor(method, parent);
		}
		catch (NoSuchMethodException e) {
			throw new IllegalStateException("Failed to get method with name '" + methodName + "'.", e);
		}
	}

	private static Class<?> loadClass(String name) {
		try {
			// TODO Use correct classloader
			// TODO Add support for primitive types and arrays.
			return JavaMethodTestDescriptor.class.getClassLoader().loadClass(name);
		}
		catch (ClassNotFoundException e) {
			throw new IllegalStateException("Failed to load class with name '" + name + "'.", e);
		}
	}

}
