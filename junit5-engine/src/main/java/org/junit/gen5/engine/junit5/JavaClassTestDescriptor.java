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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.engine.TestDescriptor;

/**
 * {@link TestDescriptor} for tests based on Java classes.
 *
 * <p>The pattern of the {@link #getTestId test ID} takes the form of
 * <code>{fully qualified class name}</code>.
 *
 * @since 5.0
 */
@Data
@EqualsAndHashCode
public class JavaClassTestDescriptor implements TestDescriptor {

	// The following pattern only supports descriptors for test methods.
	// TODO Support descriptors for test classes.
	// TODO Decide if we want to support descriptors for packages.
	private static final Pattern UID_PATTERN = Pattern.compile("^(.+):(.+)$");


	public static JavaClassTestDescriptor from(final String uid) throws RuntimeException {
		Preconditions.notNull(uid, "TestDescriptor UID must not be null");

		Matcher matcher = UID_PATTERN.matcher(uid);
		Preconditions.condition(matcher.matches(),
			() -> String.format("Invalid format for %s UID: %s", JavaClassTestDescriptor.class.getSimpleName(), uid));

		// TODO Validate contents of matched groups.
		String engineId = matcher.group(1);
		String className = matcher.group(2);

		Class<?> clazz = loadClass(className);

		return new JavaClassTestDescriptor(engineId, clazz);
	}


	private final String engineId;
	private final String testId;
	private final String displayName;
	private final TestDescriptor parent;
	private final Class<?> testClass;


	public JavaClassTestDescriptor(String engineId, Class<?> testClass) {
		Preconditions.notEmpty(engineId, "engineId must not be null or empty");
		Preconditions.notNull(testClass, "testClass must not be null");

		this.testClass = testClass;
		this.displayName = determineDisplayName(testClass);
		this.parent = null;
		this.engineId = engineId;
		this.testId = createTestId(testClass);
	}

	private static Class<?> loadClass(String name) {
		try {
			// TODO Add support for primitive types and arrays.
			return JavaClassTestDescriptor.class.getClassLoader().loadClass(name);
		}
		catch (ClassNotFoundException e) {
			throw new IllegalStateException("Failed to load class with name '" + name + "'.", e);
		}
	}

	private static String createTestId(Class<?> testClass) {
		return testClass.getName();
	}

	private static String determineDisplayName(Class<?> testClass) {
		return testClass.getSimpleName();
	}

}
