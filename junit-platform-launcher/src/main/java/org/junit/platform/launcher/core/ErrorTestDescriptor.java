/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher.core;

import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;

class ErrorTestDescriptor extends AbstractTestDescriptor {

	private final Throwable cause;

	ErrorTestDescriptor(UniqueId uniqueId, String displayName, Throwable cause) {
		super(uniqueId, displayName);
		this.cause = cause;
	}

	public Throwable getCause() {
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
