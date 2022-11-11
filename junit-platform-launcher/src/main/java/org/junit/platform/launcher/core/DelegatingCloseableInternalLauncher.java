/*
 * Copyright 2015-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher.core;

/**
 * @since 1.10
 */
class DelegatingCloseableInternalLauncher<T extends InternalLauncher> extends DelegatingInternalLauncher<T>
		implements CloseableInternalLauncher {

	private final Runnable onClose;

	public DelegatingCloseableInternalLauncher(T delegate, Runnable onClose) {
		super(delegate);
		this.onClose = onClose;
	}

	@Override
	public final void close() {
		onClose.run();
	}
}
