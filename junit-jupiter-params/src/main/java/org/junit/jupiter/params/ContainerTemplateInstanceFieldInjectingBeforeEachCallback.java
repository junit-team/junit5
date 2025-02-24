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

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

class ContainerTemplateInstanceFieldInjectingBeforeEachCallback implements BeforeEachCallback {

	private final ParameterizedContainerClassContext classContext;
	private final EvaluatedArgumentSet arguments;
	private final int invocationIndex;

	ContainerTemplateInstanceFieldInjectingBeforeEachCallback(ParameterizedContainerClassContext classContext,
			EvaluatedArgumentSet arguments, int invocationIndex) {
		this.classContext = classContext;
		this.arguments = arguments;
		this.invocationIndex = invocationIndex;
	}

	@Override
	public void beforeEach(ExtensionContext extensionContext) throws Exception {
		extensionContext.getTestInstance() //
				.ifPresent(testInstance -> this.classContext.getResolverFacade() //
						.resolveAndInjectFields(testInstance, extensionContext, this.arguments, this.invocationIndex));
	}

}
