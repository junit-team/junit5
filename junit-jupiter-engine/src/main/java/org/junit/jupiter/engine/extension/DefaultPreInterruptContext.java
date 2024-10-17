/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.extension;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.PreInterruptContext;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ToStringBuilder;

/**
 * @since 5.12
 */
class DefaultPreInterruptContext implements PreInterruptContext {
	private final Thread threadToInterrupt;
	private final ExtensionContext extensionContext;

	DefaultPreInterruptContext(Thread threadToInterrupt, ExtensionContext extensionContext) {
		Preconditions.notNull(threadToInterrupt, "threadToInterrupt must not be null");
		Preconditions.notNull(extensionContext, "ExtensionContext must not be null");
		this.threadToInterrupt = threadToInterrupt;
		this.extensionContext = extensionContext;
	}

	@Override
	public Thread getThreadToInterrupt() {
		return threadToInterrupt;
	}

	@Override
	public ExtensionContext getExtensionContext() {
		return extensionContext;
	}

	@Override
	public String toString() {
		// @formatter:off
		return new ToStringBuilder(this)
				.append("threadToInterrupt", this.threadToInterrupt)
				.append("extensionContext", this.extensionContext)
				.toString();
		// @formatter:on
	}
}
