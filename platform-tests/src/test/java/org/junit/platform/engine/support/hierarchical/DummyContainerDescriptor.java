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
public class DummyContainerDescriptor extends AbstractTestDescriptor implements Node<DummyEngineExecutionContext> {

	private final Runnable beforeBlock;
	private String skippedReason;
	private boolean skipped;

	DummyContainerDescriptor(UniqueId uniqueId, String displayName, Runnable executeBlock) {
		this(uniqueId, displayName, null, executeBlock);
	}

	public DummyContainerDescriptor(UniqueId uniqueId, String displayName, TestSource source, Runnable beforeBlock) {
		super(uniqueId, displayName);

		if (source != null) {
			setSource(source);
		}
		this.beforeBlock = beforeBlock;
	}

	@Override
	public boolean isTest() {
		return false;
	}

	@Override
	public boolean isLeaf() {
		return false;
	}

	@Override
	public boolean hasTests() {
		return true;
	}

	@Override
	public boolean isContainer() {
		return true;
	}

	public void markSkipped(String reason) {
		this.skipped = true;
		this.skippedReason = reason;
	}

	@Override
	public SkipResult shouldBeSkipped(DummyEngineExecutionContext context) throws Exception {
		return this.skipped ? skip(this.skippedReason) : doNotSkip();
	}

	@Override
	public DummyEngineExecutionContext before(DummyEngineExecutionContext context) throws Exception {
		if (this.beforeBlock != null) {
			this.beforeBlock.run();
		}
		return context;
	}

}
