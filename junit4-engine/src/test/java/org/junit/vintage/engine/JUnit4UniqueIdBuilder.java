/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.vintage.engine;

import org.junit.gen5.engine.UniqueId;
import org.junit.vintage.engine.descriptor.JUnit4TestDescriptor;

/**
 * Test data builder for building unique IDs for the JUnit4TestEngine.
 *
 * Used to decouple tests from concrete unique ID strings.
 *
 * @since 5.0
 */
public class JUnit4UniqueIdBuilder {

	public static UniqueId uniqueIdForErrorInClass(Class<?> clazz, Class<?> failingClass) {
		return uniqueIdForClasses(clazz).append(JUnit4TestDescriptor.SEGMENT_TYPE_TEST,
			"initializationError(" + failingClass.getName() + ")");
	}

	public static UniqueId uniqueIdForClass(Class<?> clazz) {
		return uniqueIdForClasses(clazz);
	}

	public static UniqueId uniqueIdForClasses(Class<?> clazz, Class<?>... clazzes) {
		UniqueId uniqueId = uniqueIdForClass(clazz.getName());
		for (Class<?> each : clazzes) {
			uniqueId = uniqueId.append(JUnit4TestDescriptor.SEGMENT_TYPE_TEST, each.getName());
		}
		return uniqueId;
	}

	public static UniqueId uniqueIdForClass(String fullyQualifiedClassName) {
		UniqueId containerId = engineId();
		return containerId.append(JUnit4TestDescriptor.SEGMENT_TYPE_RUNNER, fullyQualifiedClassName);
	}

	public static UniqueId uniqueIdForMethod(Class<?> testClass, String methodName) {
		return uniqueIdForClass(testClass).append(JUnit4TestDescriptor.SEGMENT_TYPE_TEST,
			methodValue(testClass, methodName));
	}

	private static String methodValue(Class<?> testClass, String methodName) {
		return methodName + "(" + testClass.getName() + ")";
	}

	public static UniqueId uniqueIdForMethod(Class<?> testClass, String methodName, String index) {
		return uniqueIdForClass(testClass).append(JUnit4TestDescriptor.SEGMENT_TYPE_TEST,
			methodValue(testClass, methodName) + "[" + index + "]");
	}

	public static UniqueId uniqueIdForMethod(UniqueId containerId, Class<?> testClass, String methodName) {
		return containerId.append(JUnit4TestDescriptor.SEGMENT_TYPE_TEST, methodValue(testClass, methodName));
	}

	public static UniqueId engineId() {
		return UniqueId.forEngine(JUnit4TestDescriptor.ENGINE_ID);
	}

}
