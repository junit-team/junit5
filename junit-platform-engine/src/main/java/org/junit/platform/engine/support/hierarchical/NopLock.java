/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.support.hierarchical;

import java.util.Optional;

import org.junit.platform.commons.annotation.ExecutionMode;

public class NopLock implements AcquiredResourceLock {
	public static final ResourceLock INSTANCE = new NopLock();

	private NopLock() {
	}

	@Override
	public Optional<ExecutionMode> getForcedExecutionMode() {
		return Optional.empty();
	}

	@Override
	public AcquiredResourceLock acquire() {
		return this;
	}

	@Override
	public void release() {

	}
}
