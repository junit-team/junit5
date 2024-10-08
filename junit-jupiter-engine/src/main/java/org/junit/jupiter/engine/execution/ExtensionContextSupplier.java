/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.execution;

import static org.apiguardian.api.API.Status.INTERNAL;

import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.EnableTestScopedConstructorContext;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.util.AnnotationUtils;

/**
 * Container of two instances of {@link ExtensionContext} to simplify the legacy for
 * <a href="https://github.com/junit-team/junit5/issues/3445">#3445 (Introduction of Test-scoped ExtensionContext)</a>.
 *
 * @since 5.12
 */
@API(status = INTERNAL, since = "5.12")
public final class ExtensionContextSupplier {

	private final ExtensionContext currentExtensionContext;
	private final ExtensionContext legacyExtensionContext;

	public ExtensionContextSupplier(ExtensionContext currentExtensionContext, ExtensionContext legacyExtensionContext) {
		this.currentExtensionContext = currentExtensionContext;
		this.legacyExtensionContext = legacyExtensionContext;
	}

	public ExtensionContext get(Extension extension) {
		if (currentExtensionContext == legacyExtensionContext
				|| AnnotationUtils.isAnnotated(extension.getClass(), EnableTestScopedConstructorContext.class)) {
			return currentExtensionContext;
		}
		else {
			return legacyExtensionContext;
		}
	}
}
