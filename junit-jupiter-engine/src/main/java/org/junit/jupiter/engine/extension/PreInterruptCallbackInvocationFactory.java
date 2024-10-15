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

import java.util.List;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.PreInterruptCallback;
import org.junit.jupiter.api.extension.PreInterruptContext;
import org.junit.platform.commons.util.UnrecoverableExceptions;

/**
 * @since 5.12
 * @see PreInterruptCallbackInvocation
 */
final class PreInterruptCallbackInvocationFactory {

	private PreInterruptCallbackInvocationFactory() {
	}

	static PreInterruptCallbackInvocation create(ExtensionContext extensionContext) {
		final List<PreInterruptCallback> callbacks = extensionContext.getExtensions(PreInterruptCallback.class);
		if (callbacks.isEmpty()) {
			return PreInterruptCallbackInvocation.NOOP;
		}
		return (thread, errorHandler) -> {
			PreInterruptContext preInterruptContext = new DefaultPreInterruptContext(thread, extensionContext);
			for (PreInterruptCallback callback : callbacks) {
				try {
					callback.beforeThreadInterrupt(preInterruptContext);
				}
				catch (Throwable ex) {
					UnrecoverableExceptions.rethrowIfUnrecoverable(ex);
					errorHandler.accept(ex);
				}
			}
		};
	}
}
