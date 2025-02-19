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

import static java.util.Collections.singletonList;

import java.util.List;

import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.params.provider.Arguments;

/**
 * @since 5.0
 */
class ParameterizedTestInvocationContext extends ParameterizedInvocationContext<ParameterizedTestMethodContext>
		implements TestTemplateInvocationContext {

	ParameterizedTestInvocationContext(ParameterizedInvocationNameFormatter formatter,
			ParameterizedTestMethodContext methodContext, Arguments arguments, int invocationIndex) {
		super(formatter, methodContext, arguments, invocationIndex);
	}

	@Override
	public String getDisplayName(int invocationIndex) {
		return super.getDisplayName(invocationIndex);
	}

	@Override
	public List<Extension> getAdditionalExtensions() {
		return singletonList( //
			new TestTemplateMethodParameterResolver(this.declarationContext, this.arguments, this.invocationIndex) //
		);
	}

	@Override
	public void prepareInvocation(ExtensionContext context) {
		super.prepareInvocation(context);
	}

}
