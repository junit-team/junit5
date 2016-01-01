/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine;

class DummyTestDescriptor extends AbstractTestDescriptor implements Leaf<DummyEngineExecutionContext> {

	private final String displayName;
	private final Runnable runnable;

	DummyTestDescriptor(String uniqueId, String displayName, Runnable runnable) {
		super(uniqueId);
		this.displayName = displayName;
		this.runnable = runnable;
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}

	@Override
	public boolean isTest() {
		return true;
	}

	@Override
	public boolean isContainer() {
		return false;
	}

	@Override
	public DummyEngineExecutionContext execute(DummyEngineExecutionContext context) {
		runnable.run();
		return context;
	}

}
