/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit4;

import lombok.Value;

import org.junit.gen5.engine.TestDescriptor;
import org.junit.runner.Description;

@Value
final class DescriptionTestDescriptor implements TestDescriptor {

	private TestDescriptor parent;
	private Description description;

	@Override
	public boolean isTest() {
		return description.isTest();
	}

	@Override
	public String getUniqueId() {
		// TODO Use unique ID if set, too
		return "junit4:" + description.getDisplayName();
	}

	@Override
	public TestDescriptor getParent() {
		return parent;
	}

	@Override
	public String getDisplayName() {
		return description.getDisplayName();
	}
}