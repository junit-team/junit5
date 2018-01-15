/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.support.hierarchical;

import org.junit.platform.commons.annotation.ExecutionMode;

import java.util.Optional;

public class NopLock implements ResourceLock {
	public static final ResourceLock INSTANCE = new NopLock();

	private NopLock() {
	}

	@Override
	public Optional<ExecutionMode> getForcedExecutionMode() {
		return Optional.empty();
	}

	@Override
	public ResourceLock acquire() {
		return this;
	}

	@Override
	public void release() {

	}
}
