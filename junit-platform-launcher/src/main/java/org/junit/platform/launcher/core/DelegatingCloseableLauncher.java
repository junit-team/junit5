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

import java.util.function.Function;

import org.junit.platform.launcher.Launcher;

/**
 * @since 1.10
 */
class DelegatingCloseableLauncher<T extends Launcher> extends DelegatingLauncher<T> implements CloseableLauncher {

	private final Function<? super T, ? extends T> onClose;
	private boolean closed;

	public DelegatingCloseableLauncher(T delegate, Function<? super T, ? extends T> onClose) {
		super(delegate);
		this.onClose = onClose;
	}

	@Override
	public final void close() {
		if (!closed) {
			this.closed = true;
			delegate = this.onClose.apply(this.delegate);
		}
	}

	public boolean isClosed() {
		return closed;
	}
}
