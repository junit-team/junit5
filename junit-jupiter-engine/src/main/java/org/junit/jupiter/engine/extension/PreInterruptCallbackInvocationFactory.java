/*
 * Copyright 2015-2023 the original author or authors.
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
import org.junit.platform.commons.util.UnrecoverableExceptions;

/**
 * @since 5.11
 */
final class PreInterruptCallbackInvocationFactory {

	private PreInterruptCallbackInvocationFactory() {

	}

	static PreInterruptCallbackInvocation create(ExtensionContext extensionContext) {
		ExtensionRegistry registry = MutableExtensionRegistry.getRegistryFromExtensionContext(extensionContext);
		if (registry == null) {
			return PreInterruptCallbackInvocation.NOOP;
		}
		List<PreInterruptCallback> callbacks = registry.getExtensions(PreInterruptCallback.class);
		return (thread, errorHandler) -> {
			for (PreInterruptCallback callback : callbacks) {
				try {
					callback.beforeThreadInterrupt(thread, extensionContext);
				}
				catch (Throwable ex) {
					UnrecoverableExceptions.rethrowIfUnrecoverable(ex);
					errorHandler.accept(ex);
				}
			}
		};
	}
}
