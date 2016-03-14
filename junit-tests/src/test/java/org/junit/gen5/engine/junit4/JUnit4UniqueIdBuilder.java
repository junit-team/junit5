/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit4;

import org.junit.gen5.engine.UniqueId;
import org.junit.gen5.engine.junit4.descriptor.JUnit4TestDescriptor;

/**
 * Test data builder for building unique IDs for the JUnit4TestEngine.
 *
 * Used to decouple tests from concrete unique ID strings.
 */
public class JUnit4UniqueIdBuilder {

	public static String uniqueIdForErrorInClass(Class<?> clazz, Class<?> failingClass) {
		return uniqueIdForClasses(clazz) + "/initializationError(" + failingClass.getName() + ")";
	}

	public static String uniqueIdForClass(Class<?> clazz) {
		return uniqueIdForClasses(clazz);
	}

	public static String uniqueIdForClasses(Class<?> clazz, Class<?>... clazzes) {
		UniqueId containerId = engineId();
		String uniqueId = containerId.getUniqueString() + ":" + clazz.getName();
		for (Class<?> each : clazzes) {
			uniqueId += "/" + each.getName();
		}
		return uniqueId;
	}

	public static String uniqueIdForClass(String fullyQualifiedClassName) {
		UniqueId containerId = engineId();
		return containerId.getUniqueString() + ":" + fullyQualifiedClassName;
	}

	public static String uniqueIdForMethod(Class<?> testClass, String methodName) {
		return uniqueIdForClass(testClass) + "/" + methodName + "(" + testClass.getName() + ")";
	}

	public static String uniqueIdForMethod(String containerId, Class<?> testClass, String methodName) {
		return containerId + "/" + methodName + "(" + testClass.getName() + ")";
	}

	public static UniqueId engineId() {
		return UniqueId.forEngine(JUnit4TestDescriptor.ENGINE_ID);
	}

}
