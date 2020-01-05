/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
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
public class DemoHierarchicalContainerDescriptor extends AbstractTestDescriptor
		implements Node<DemoEngineExecutionContext> {

	private final Runnable beforeBlock;
	private String skippedReason;
	private boolean skipped;

	DemoHierarchicalContainerDescriptor(UniqueId uniqueId, String displayName, Runnable executeBlock) {
		this(uniqueId, displayName, null, executeBlock);
	}

	public DemoHierarchicalContainerDescriptor(UniqueId uniqueId, String displayName, TestSource source,
			Runnable beforeBlock) {
		super(uniqueId, displayName, source);
		this.beforeBlock = beforeBlock;
	}

	@Override
	public Type getType() {
		return Type.CONTAINER;
	}

	@Override
	public boolean mayRegisterTests() {
		return true;
	}

	public void markSkipped(String reason) {
		this.skipped = true;
		this.skippedReason = reason;
	}

	@Override
	public SkipResult shouldBeSkipped(DemoEngineExecutionContext context) {
		return this.skipped ? skip(this.skippedReason) : doNotSkip();
	}

	@Override
	public DemoEngineExecutionContext before(DemoEngineExecutionContext context) {
		if (this.beforeBlock != null) {
			this.beforeBlock.run();
		}
		return context;
	}

}
