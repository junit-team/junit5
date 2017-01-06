/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine.discovery;

import org.junit.jupiter.engine.JupiterTestEngine;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.engine.UniqueId;

/**
 * Test data builder for unique IDs for JupiterTestEngine.
 *
 * Used to decouple tests from concrete unique ID strings.
 *
 * @since 5.0
 */
public class JupiterUniqueIdBuilder {

	public static UniqueId uniqueIdForClass(Class<?> clazz) {
		UniqueId containerId = engineId();
		if (clazz.getEnclosingClass() != null && !ReflectionUtils.isStatic(clazz)) {
			containerId = uniqueIdForClass(clazz.getEnclosingClass());
			return containerId.append(NestedTestsResolver.SEGMENT_TYPE, clazz.getSimpleName());
		}
		return containerId.append(TestContainerResolver.SEGMENT_TYPE, clazz.getName());
	}

	public static UniqueId uniqueIdForTopLevelClass(String className) {
		return engineId().append(TestContainerResolver.SEGMENT_TYPE, className);
	}

	public static UniqueId uniqueIdForMethod(Class<?> clazz, String methodPart) {
		return uniqueIdForClass(clazz).append(TestMethodResolver.SEGMENT_TYPE, methodPart);
	}

	public static UniqueId uniqueIdForTestFactoryMethod(Class<?> clazz, String methodPart) {
		return uniqueIdForClass(clazz).append(TestFactoryMethodResolver.SEGMENT_TYPE, methodPart);
	}

	public static UniqueId uniqueIdForTestTemplateMethod(Class<?> clazz, String methodPart) {
		return uniqueIdForClass(clazz).append(TestTemplateMethodResolver.SEGMENT_TYPE, methodPart);
	}

	public static UniqueId engineId() {
		return UniqueId.forEngine(JupiterTestEngine.ENGINE_ID);
	}

}
