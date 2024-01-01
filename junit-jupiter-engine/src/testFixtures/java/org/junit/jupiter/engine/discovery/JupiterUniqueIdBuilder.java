/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.discovery;

import static org.junit.platform.commons.util.ReflectionUtils.isInnerClass;

import org.junit.jupiter.engine.descriptor.ClassTestDescriptor;
import org.junit.jupiter.engine.descriptor.JupiterEngineDescriptor;
import org.junit.jupiter.engine.descriptor.NestedClassTestDescriptor;
import org.junit.jupiter.engine.descriptor.TestFactoryTestDescriptor;
import org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor;
import org.junit.jupiter.engine.descriptor.TestTemplateInvocationTestDescriptor;
import org.junit.jupiter.engine.descriptor.TestTemplateTestDescriptor;
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
		if (isInnerClass(clazz)) {
			containerId = uniqueIdForClass(clazz.getEnclosingClass());
			return containerId.append(NestedClassTestDescriptor.SEGMENT_TYPE, clazz.getSimpleName());
		}
		return containerId.append(ClassTestDescriptor.SEGMENT_TYPE, clazz.getName());
	}

	public static UniqueId uniqueIdForTopLevelClass(String className) {
		return engineId().append(ClassTestDescriptor.SEGMENT_TYPE, className);
	}

	public static UniqueId uniqueIdForMethod(Class<?> clazz, String methodPart) {
		return uniqueIdForClass(clazz).append(TestMethodTestDescriptor.SEGMENT_TYPE, methodPart);
	}

	public static UniqueId uniqueIdForTestFactoryMethod(Class<?> clazz, String methodPart) {
		return uniqueIdForClass(clazz).append(TestFactoryTestDescriptor.SEGMENT_TYPE, methodPart);
	}

	public static UniqueId uniqueIdForTestTemplateMethod(Class<?> clazz, String methodPart) {
		return uniqueIdForClass(clazz).append(TestTemplateTestDescriptor.SEGMENT_TYPE, methodPart);
	}

	public static UniqueId appendTestTemplateInvocationSegment(UniqueId parentId, int index) {
		return parentId.append(TestTemplateInvocationTestDescriptor.SEGMENT_TYPE, "#" + index);
	}

	public static UniqueId engineId() {
		return UniqueId.forEngine(JupiterEngineDescriptor.ENGINE_ID);
	}

}
