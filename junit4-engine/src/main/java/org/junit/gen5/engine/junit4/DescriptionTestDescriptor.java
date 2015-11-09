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

import java.util.Collections;
import java.util.Set;

import org.junit.gen5.engine.AbstractTestDescriptor;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestTag;
import org.junit.runner.Description;

class DescriptionTestDescriptor extends AbstractTestDescriptor {

	private final Description description;

	DescriptionTestDescriptor(TestDescriptor parent, Description description) {
		super("junit4:" + description.getDisplayName());
		parent.addChild(this);
		this.description = description;
	}

	@Override
	public boolean isTest() {
		return description.isTest();
	}

	@Override
	public Set<TestTag> getTags() {
		return Collections.emptySet();
	}

	@Override
	public String getDisplayName() {
		return description.getDisplayName();
	}

	public Description getDescription() {
		return description;
	}
}