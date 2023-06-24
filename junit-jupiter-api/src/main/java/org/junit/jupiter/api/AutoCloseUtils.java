/*
 * Copyright 2015-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api;

import java.io.Closeable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.apiguardian.api.API;

/**
 * The {@code AutoCloseUtils} class provides utility methods for automatically closing resources used in JUnit 5 tests.
 *
 * <p>
 * The class includes a static method {@code closeResources} that accepts a test instance as a parameter and closes
 * all the fields annotated with {@link org.junit.jupiter.api.AutoClose}. This allows for automatic resource cleanup
 * after test execution.
 * </p>
 *
 * <p>
 * The {@code closeResources} method utilizes reflection to find the fields annotated with {@link org.junit.jupiter.api.AutoClose}
 * within the given test instance. It collects all the closeable fields and invokes the {@code close} method on each one.
 * </p>
 *
 * <p>
 * To be eligible for automatic closing, the fields must implement the {@link java.io.Closeable} interface.
 * </p>
 *
 * <p>
 * The {@code AutoCloseUtils} class is not intended to be instantiated, as all its methods are static.
 * </p>
 *
 * @see org.junit.jupiter.api.AutoClose
 * @see java.io.Closeable
 * @see java.lang.reflect.Field
 */
@API(status = API.Status.EXPERIMENTAL, since = "5.9")
public class AutoCloseUtils {
	private AutoCloseUtils() {
		// Private constructor to prevent instantiation
	}

	/**
	 * Closes all the resources annotated with {@link org.junit.jupiter.api.AutoClose} within the given test instance.
	 *
	 * @param testInstance the test instance containing the resources to be closed
	 */
	public static void closeResources(Object testInstance) {
		List<Closeable> closeables = findCloseableFields(testInstance);
		for (Closeable closeable : closeables) {
			close(closeable);
		}
	}

	private static List<Closeable> findCloseableFields(Object testInstance) {
		List<Closeable> closeables = new ArrayList<>();
		Field[] fields = testInstance.getClass().getDeclaredFields();
		for (Field field : fields) {
			if (field.isAnnotationPresent(AutoClose.class)) {
				field.setAccessible(true);
				try {
					Object fieldValue = field.get(testInstance);
					if (fieldValue instanceof Closeable) {
						closeables.add((Closeable) fieldValue);
					}
				}
				catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
		return closeables;
	}

	private static void close(Closeable closeable) {
		try {
			closeable.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
