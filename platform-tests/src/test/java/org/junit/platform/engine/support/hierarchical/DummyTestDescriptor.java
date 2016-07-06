/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.engine.support.hierarchical;

import static org.junit.platform.engine.support.hierarchical.Node.SkipResult.doNotSkip;
import static org.junit.platform.engine.support.hierarchical.Node.SkipResult.skip;

import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;

/**
 * @since 1.0
 */
public class DummyTestDescriptor extends AbstractTestDescriptor implements Node<DummyEngineExecutionContext> {

	private final Runnable runnable;
	private String skippedReason;
	private boolean skipped;

	DummyTestDescriptor(UniqueId uniqueId, String displayName, Runnable runnable) {
		this(uniqueId, displayName, null, runnable);
	}

	public DummyTestDescriptor(UniqueId uniqueId, String displayName, TestSource source, Runnable runnable) {
		super(uniqueId, displayName);
		if (source != null) {
			setSource(source);
		}
		this.runnable = runnable;
	}

	@Override
	public boolean isTest() {
		return !isContainer();
	}

	@Override
	public boolean isContainer() {
		return runnable == null;
	}

	@Override
	public boolean isLeaf() {
		return isTest();
	}

	public void markSkipped(String reason) {
		this.skipped = true;
		this.skippedReason = reason;
	}

	@Override
	public SkipResult shouldBeSkipped(DummyEngineExecutionContext context) throws Exception {
		return skipped ? skip(skippedReason) : doNotSkip();
	}

	@Override
	public DummyEngineExecutionContext execute(DummyEngineExecutionContext context) {
		if (runnable != null) {
			runnable.run();
		}
		return context;
	}

}
