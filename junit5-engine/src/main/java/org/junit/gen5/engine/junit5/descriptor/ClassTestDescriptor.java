/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.descriptor;

import lombok.Data;

import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.engine.AbstractTestDescriptor;
import org.junit.gen5.engine.TestDescriptor;

/**
 * {@link TestDescriptor} for tests based on Java classes.
 *
 * <p>The pattern of the {@link #getUniqueId unique ID} takes the form of
 * <code>{parent unique id}:{fully qualified class name}</code>.
 *
 * @since 5.0
 */
@Data
public class ClassTestDescriptor extends AbstractTestDescriptor {

	private final Class<?> testClass;

	public ClassTestDescriptor(String uniqueId, Class<?> testClass) {
		super(uniqueId);
		Preconditions.notNull(testClass, "testClass must not be null");

		this.testClass = testClass;
	}

	@Override
	public final String getDisplayName() {
		return this.testClass.getSimpleName();
	}

	@Override
	public final boolean isTest() {
		return false;
	}

}
