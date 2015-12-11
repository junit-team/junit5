/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine;

class DummyTestDescriptor extends AbstractTestDescriptor implements Leaf<DummyEngineExecutionContext> {

	public static final String ENGINE_ID = "dummy";

	private final String displayName;
	private final Runnable runnable;

	DummyTestDescriptor(String displayName, Runnable runnable) {
		super(ENGINE_ID + ":" + displayName);
		this.displayName = displayName;
		this.runnable = runnable;
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}

	@Override
	public boolean isTest() {
		return getChildren().isEmpty();
	}

	@Override
	public boolean isContainer() {
		return !isTest();
	}

	@Override
	public DummyEngineExecutionContext execute(DummyEngineExecutionContext context) throws Throwable {
		if (runnable != null) {
			runnable.run();
		}
		return context;
	}

}
