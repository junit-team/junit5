/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit4.descriptor;

import org.junit.gen5.engine.AbstractTestDescriptor;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.runner.Description;

/**
 * @since 5.0
 */
public class JUnit4TestDescriptor extends AbstractTestDescriptor {

	private final Description description;

	public JUnit4TestDescriptor(TestDescriptor parent, char separator, String uniqueIdSuffix, Description description) {
		super(parent.getUniqueId() + separator + uniqueIdSuffix);
		this.description = description;
	}

	public Description getDescription() {
		return description;
	}

	@Override
	public String getDisplayName() {
		String methodName = description.getMethodName();
		return methodName != null ? methodName : description.getDisplayName();
	}

	@Override
	public boolean isTest() {
		return description.isTest();
	}

	@Override
	public boolean isContainer() {
		return description.isSuite();
	}

}
