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

	public static String uniqueIdForClass(Class<?> clazz) {
		UniqueId containerId = engineId();
		return containerId.getUniqueString(); //TODO add class stuff
	}

	public static String uniqueIdForMethod(Class<?> clazz, String methodPart) {
		UniqueId containerId = engineId();
		return containerId.getUniqueString(); //TODO add method stuff
	}

	public static UniqueId engineId() {
		return UniqueId.forEngine(JUnit4TestDescriptor.ENGINE_ID);
	}

}
