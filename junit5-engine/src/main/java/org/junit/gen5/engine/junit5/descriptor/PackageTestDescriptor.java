/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.descriptor;

import org.junit.gen5.engine.junit5.execution.JUnit5EngineExecutionContext;
import org.junit.gen5.engine.support.hierarchical.Container;

public class PackageTestDescriptor extends JUnit5TestDescriptor implements Container<JUnit5EngineExecutionContext> {
	private final String packageName;

	public PackageTestDescriptor(String uniqueId, String packageName) {
		super(uniqueId);
		this.packageName = packageName;
		// TODO add valid TestSource
	}

	public String getPackageName() {
		return packageName;
	}

	@Override
	public String getName() {
		return packageName;
	}

	@Override
	public String getDisplayName() {
		return packageName;
	}

	@Override
	public boolean isTest() {
		return false;
	}

	@Override
	public boolean isContainer() {
		return true;
	}
}
