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

public class UniqueIdBuilder {

	public static UniqueId uniqueIdForClass(Class<?> clazz) {
		UniqueId containerId = engineId();
		if (clazz.getEnclosingClass() != null && !ReflectionUtils.isStatic(clazz)) {
			containerId = uniqueIdForClass(clazz.getEnclosingClass());
			return containerId.append(JUnit5TestableFactory.TYPE_NESTED_CLASS, clazz.getSimpleName());
		}
		return containerId.append(JUnit5TestableFactory.TYPE_CLASS, clazz.getName());
	}

	public static UniqueId uniqueIdForMethod(Class<?> clazz, String methodPart) {
		return uniqueIdForClass(clazz).append(JUnit5TestableFactory.TYPE_METHOD, methodPart);
	}

	public static UniqueId engineId() {
		return UniqueId.forEngine(JUnit5TestEngine.ENGINE_ID);
	}

}
