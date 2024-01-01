/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher.core;

import org.junit.platform.engine.TestEngine;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.descriptor.ClassSource;

/**
 * Represents an error thrown by a {@link org.junit.platform.engine.TestEngine}
 * during discovery.
 *
 * <p>The contained {@link Throwable} will be reported as the cause of a test
 * failure by the {@link DefaultLauncher} when execution is started for this
 * engine.
 *
 * @since 1.6
 */
class EngineDiscoveryErrorDescriptor extends AbstractTestDescriptor {

	private final Throwable cause;

	EngineDiscoveryErrorDescriptor(UniqueId uniqueId, TestEngine testEngine, Throwable cause) {
		super(uniqueId, testEngine.getId(), ClassSource.from(testEngine.getClass()));
		this.cause = cause;
	}

	Throwable getCause() {
		return cause;
	}

	@Override
	public Type getType() {
		return Type.TEST;
	}

	@Override
	public void prune() {
		// prevent pruning
	}

}
