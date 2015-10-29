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

	private final String engineId;
	private final TestDescriptor parent;
	private final Class<?> testClass;


	public JavaClassTestDescriptor(String engineId, Class<?> testClass) {
		Preconditions.notEmpty(engineId, "engineId must not be null or empty");
		Preconditions.notNull(testClass, "testClass must not be null");

		this.testClass = testClass;
		//todo there could be parents (eg. packages)
		this.parent = null;
		this.engineId = engineId;
	}

	@Override
	public String getTestId() {
		return testClass.getName();
	}

	@Override
	public String getDisplayName() {
		return testClass.getSimpleName();
	}

	@Override
	public boolean isTest() {
		return false;
	}

}
