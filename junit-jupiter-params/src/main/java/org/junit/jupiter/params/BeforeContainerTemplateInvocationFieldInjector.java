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

import org.junit.jupiter.api.extension.BeforeContainerTemplateInvocationCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

class BeforeContainerTemplateInvocationFieldInjector implements BeforeContainerTemplateInvocationCallback {

	private final ResolverFacade resolverFacade;
	private final EvaluatedArgumentSet arguments;
	private final int invocationIndex;
	private final ResolutionCache resolutionCache;

	BeforeContainerTemplateInvocationFieldInjector(ResolverFacade resolverFacade, EvaluatedArgumentSet arguments,
			int invocationIndex, ResolutionCache resolutionCache) {
		this.resolverFacade = resolverFacade;
		this.arguments = arguments;
		this.invocationIndex = invocationIndex;
		this.resolutionCache = resolutionCache;
	}

	@Override
	public void beforeContainerTemplateInvocation(ExtensionContext extensionContext) {
		extensionContext.getTestInstance() //
				.ifPresent(testInstance -> this.resolverFacade //
						.resolveAndInjectFields(testInstance, extensionContext, this.arguments, this.invocationIndex,
							this.resolutionCache));
	}

}
