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

import static java.util.Collections.emptySet;

import java.util.Set;

import org.junit.gen5.engine.AbstractTestDescriptor;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestTag;
import org.junit.runner.Description;

abstract class JUnit4TestDescriptor extends AbstractTestDescriptor {

	public static String ENGINE_ID = "junit4";

	protected JUnit4TestDescriptor(String uniqueId) {
		super(uniqueId);
	}

	@Override
	public String getDisplayName() {
		return getDescription().getDisplayName();
	}

	@Override
	public boolean isTest() {
		return getDescription().isTest();
	}

	@Override
	public Set<TestTag> getTags() {
		return emptySet();
	}

	public abstract Description getDescription();

}
