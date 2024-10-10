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
import static org.junit.jupiter.api.extension.TestInstantiationAwareExtension.ExtensionContextScope.TEST_METHOD;

import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstantiationAwareExtension;

/**
 * Container of two instances of {@link ExtensionContext} to simplify the legacy for
 * <a href="https://github.com/junit-team/junit5/issues/3445">#3445</a>.
 *
 * @since 5.12
 * @see TestInstantiationAwareExtension
 */
@FunctionalInterface
@API(status = INTERNAL, since = "5.12")
public interface ExtensionContextSupplier {

	static ExtensionContextSupplier create(ExtensionContext currentExtensionContext,
			ExtensionContext legacyExtensionContext) {
		if (currentExtensionContext == legacyExtensionContext) {
			return __ -> currentExtensionContext;
		}
		return new ScopeBasedExtensionContextSupplier(currentExtensionContext, legacyExtensionContext);
	}

	ExtensionContext get(TestInstantiationAwareExtension extension);

	class ScopeBasedExtensionContextSupplier implements ExtensionContextSupplier {

		private final ExtensionContext currentExtensionContext;
		private final ExtensionContext legacyExtensionContext;

		private ScopeBasedExtensionContextSupplier(ExtensionContext currentExtensionContext,
				ExtensionContext legacyExtensionContext) {
			this.currentExtensionContext = currentExtensionContext;
			this.legacyExtensionContext = legacyExtensionContext;
		}

		public ExtensionContext get(TestInstantiationAwareExtension extension) {
			return isTestScoped(extension) ? currentExtensionContext : legacyExtensionContext;
		}

		private boolean isTestScoped(TestInstantiationAwareExtension extension) {
			ExtensionContext rootContext = legacyExtensionContext.getRoot();
			return extension.getTestInstantiationExtensionContextScope(rootContext) == TEST_METHOD;
		}
	}
}
