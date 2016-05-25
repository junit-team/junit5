/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.support.hierarchical;

import static org.junit.gen5.engine.support.hierarchical.Node.SkipResult.dontSkip;
import static org.junit.gen5.engine.support.hierarchical.Node.SkipResult.skip;

import org.junit.gen5.engine.UniqueId;
import org.junit.gen5.engine.support.descriptor.AbstractTestDescriptor;

/**
 * @since 5.0
 */
public class DummyTestDescriptor extends AbstractTestDescriptor
		implements Leaf<DummyEngineExecutionContext>, Node<DummyEngineExecutionContext> {

	private String displayName;
	private final Runnable runnable;
	private String skippedReason;
	private boolean skipped;

	DummyTestDescriptor(UniqueId uniqueId, String displayName, Runnable runnable) {
		super(uniqueId);
		this.displayName = displayName;
		this.runnable = runnable;
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	@Override
	public boolean isTest() {
		return true;
	}

	@Override
	public boolean isContainer() {
		return false;
	}

	public void markSkipped(String reason) {
		this.skipped = true;
		this.skippedReason = reason;
	}

	@Override
	public SkipResult shouldBeSkipped(DummyEngineExecutionContext context) throws Exception {
		return skipped ? skip(skippedReason) : dontSkip();
	}

	@Override
	public DummyEngineExecutionContext execute(DummyEngineExecutionContext context) {
		runnable.run();
		return context;
	}

}
