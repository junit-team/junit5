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

import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestTag;
import org.junit.runner.Description;

interface JUnit4TestDescriptor extends TestDescriptor {

	String ENGINE_ID = "junit4";

	@Override
	default String getUniqueId() {
		// TODO Use unique ID if set, too
		return ENGINE_ID + ":" + getDescription().getDisplayName();
	}

	@Override
	default String getDisplayName() {
		return getDescription().getDisplayName();
	}

	@Override
	default boolean isTest() {
		return getDescription().isTest();
	}

	@Override
	default Set<TestTag> getTags() {
		return emptySet();
	}

	Description getDescription();

}
