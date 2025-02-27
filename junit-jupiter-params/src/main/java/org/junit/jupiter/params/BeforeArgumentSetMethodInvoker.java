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

import org.junit.jupiter.api.extension.BeforeContainerTemplateInvocationCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

class BeforeArgumentSetMethodInvoker extends AbstractArgumentSetLifecycleMethodInvoker
		implements BeforeContainerTemplateInvocationCallback {

	BeforeArgumentSetMethodInvoker(Class<?> containerTemplateClass, Method method) {
		super(containerTemplateClass, method);
	}

	@Override
	public void beforeContainerTemplateInvocation(ExtensionContext context) {
		invoke(context);
	}

}
