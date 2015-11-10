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

import java.util.Set;

import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.engine.TestTag;

public class ContextTestDescriptor extends AbstractJUnit5TestDescriptor {

	private final String displayName;
	private final Class<?> contextClass;

	public ContextTestDescriptor(String uniqueId, Class<?> contextClass) {
		super(uniqueId);
		Preconditions.notNull(contextClass, "contextClass must not be null");

		this.contextClass = contextClass;

		this.displayName = determineDisplayName(contextClass, contextClass.getName());

	}

	public Class<?> getContextClass() {
		return contextClass;
	}

	public String getDisplayName() {
		return displayName;
	}

	@Override
	public Set<TestTag> getTags() {
		return getTags(this.contextClass);
	}

	@Override
	public final boolean isTest() {
		return false;
	}

}
