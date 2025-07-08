/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine;

import java.util.ArrayList;
import java.util.List;

import org.junit.platform.commons.util.Preconditions;

/**
 * @since 6.0
 */
final class RegularCancellationToken implements CancellationToken {

	private final List<Listener> listeners = new ArrayList<>();
	private volatile boolean cancelled = false;

	@Override
	public /* not synchronized */ boolean isCancellationRequested() {
		return cancelled;
	}

	@Override
	public synchronized void cancel() {
		if (!cancelled) {
			cancelled = true;
			listeners.forEach(it -> it.cancellationRequested(this));
			listeners.clear();
		}
	}

	@Override
	public void addListener(Listener listener) {
		Preconditions.notNull(listener, "listener must not be null");
		synchronized (this) {
			if (cancelled) {
				listener.cancellationRequested(this);
			}
			else {
				listeners.add(listener);
			}
		}
	}

	@Override
	public void removeListener(Listener listener) {
		Preconditions.notNull(listener, "listener must not be null");
		synchronized (this) {
			listeners.remove(listener);
		}
	}
}
