/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.extension;

import org.junit.jupiter.api.extension.PreInterruptContext;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ToStringBuilder;

/**
 * @since 5.12
 */
class DefaultPreInterruptContext implements PreInterruptContext {
	private final Thread threadToInterrupt;

	DefaultPreInterruptContext(Thread threadToInterrupt) {
		Preconditions.notNull(threadToInterrupt, "threadToInterrupt must not be null");
		this.threadToInterrupt = threadToInterrupt;
	}

	@Override
	public Thread getThreadToInterrupt() {
		return threadToInterrupt;
	}

	@Override
	public String toString() {
		// @formatter:off
		return new ToStringBuilder(this)
				.append("threadToInterrupt", this.threadToInterrupt)
				.toString();
		// @formatter:on
	}
}
