/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.discovery;

import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.engine.UniqueId;
import org.junit.gen5.engine.junit5.JUnit5TestEngine;
import org.junit.gen5.engine.junit5.discoveryNEW.NestedTestsResolver;
import org.junit.gen5.engine.junit5.discoveryNEW.TestContainerResolver;
import org.junit.gen5.engine.junit5.discoveryNEW.TestMethodResolver;

/**
 * Test data builder for unique IDs for JUnit5TestEngine.
 *
 * Used to decouple tests from concrete unique ID strings.
 */
public class JUnit5UniqueIdBuilder {

	public static UniqueId uniqueIdForClass(Class<?> clazz) {
		UniqueId containerId = engineId();
		if (clazz.getEnclosingClass() != null && !ReflectionUtils.isStatic(clazz)) {
			containerId = uniqueIdForClass(clazz.getEnclosingClass());
			return containerId.append(NestedTestsResolver.SEGMENT_TYPE, clazz.getSimpleName());
		}
		return containerId.append(TestContainerResolver.SEGMENT_TYPE, clazz.getName());
	}

	public static UniqueId uniqueIdForMethod(Class<?> clazz, String methodPart) {
		return uniqueIdForClass(clazz).append(TestMethodResolver.SEGMENT_TYPE, methodPart);
	}

	public static UniqueId engineId() {
		return UniqueId.forEngine(JUnit5TestEngine.ENGINE_ID);
	}

}
