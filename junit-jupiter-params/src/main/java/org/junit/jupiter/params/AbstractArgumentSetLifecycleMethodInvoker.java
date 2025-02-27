/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params;

import java.lang.reflect.Method;

import org.junit.jupiter.api.extension.ExecutableInvoker;
import org.junit.jupiter.api.extension.ExtensionContext;

abstract class AbstractArgumentSetLifecycleMethodInvoker {

	private final Class<?> containerTemplateClass;
	private final Method method;

	AbstractArgumentSetLifecycleMethodInvoker(Class<?> containerTemplateClass, Method method) {
		this.containerTemplateClass = containerTemplateClass;
		this.method = method;
	}

	protected void invoke(ExtensionContext context) {
		if (isCorrectTestClass(context)) {
			ExecutableInvoker executableInvoker = context.getExecutableInvoker();
			Object testInstance = context.getTestInstance().orElse(null);
			executableInvoker.invoke(method, testInstance);
		}
	}

	private boolean isCorrectTestClass(ExtensionContext context) {
		return this.containerTemplateClass.equals(context.getTestClass().orElse(null));
	}

}
