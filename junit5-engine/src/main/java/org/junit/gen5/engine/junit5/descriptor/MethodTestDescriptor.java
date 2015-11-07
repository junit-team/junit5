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

import java.lang.reflect.Method;
import java.util.Set;

import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestTag;

/**
 * {@link TestDescriptor} for tests based on Java methods.
 *
 * @author Sam Brannen
 * @since 5.0
 */
public class MethodTestDescriptor extends AbstractJUnit5TestDescriptor {

	private final String displayName;

	private final Method testMethod;

	public MethodTestDescriptor(String uniqueId, Method testMethod) {
		super(uniqueId);
		Preconditions.notNull(testMethod, "testMethod must not be null");

		this.testMethod = testMethod;

		this.displayName = determineDisplayName(testMethod, testMethod.getName());
	}

	@Override
	public Set<TestTag> getTags() {
		Set<TestTag> methodTags = getTags(this.getTestMethod());
		if (getParent() != null) {
			methodTags.addAll(getParent().getTags());
		}
		return methodTags;
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}

	public Method getTestMethod() {
		return testMethod;
	}

	@Override
	public final boolean isTest() {
		return true;
	}

}
