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

import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;

/**
 * @since 5.0
 */
public class DummyEngineDescriptor extends EngineDescriptor implements Node<DummyEngineExecutionContext> {

	private String skippedReason;
	private boolean skipped;
	private Runnable beforeAllBehavior = () -> {
	};

	public DummyEngineDescriptor(UniqueId uniqueId) {
		super(uniqueId, uniqueId.getEngineId().get());
	}

	public void markSkipped(String reason) {
		this.skipped = true;
		this.skippedReason = reason;
	}

	public void setBeforeAllBehavior(Runnable beforeAllBehavior) {
		this.beforeAllBehavior = beforeAllBehavior;
	}

	@Override
	public SkipResult shouldBeSkipped(DummyEngineExecutionContext context) {
		return skipped ? skip(skippedReason) : doNotSkip();
	}

	@Override
	public DummyEngineExecutionContext before(DummyEngineExecutionContext context) throws Exception {
		beforeAllBehavior.run();
		return context;
	}

	@Override
	public boolean isLeaf() {
		return false;
	}

}
