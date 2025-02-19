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

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.extension.ContainerTemplateInvocationContext;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;

class ParameterizedContainerInvocationContext extends ParameterizedInvocationContext<ParameterizedContainerClassContext>
		implements ContainerTemplateInvocationContext {

	ParameterizedContainerInvocationContext(ParameterizedContainerClassContext classContext,
			ParameterizedInvocationNameFormatter formatter, Arguments arguments, int invocationIndex) {
		super(classContext, formatter, arguments, invocationIndex);
	}

	@Override
	public String getDisplayName(int invocationIndex) {
		return super.getDisplayName(invocationIndex);
	}

	@Override
	public List<Extension> getAdditionalExtensions() {
		// TODO #878 Register either parameter resolvers or field injectors?
		return Arrays.asList( //
			new ContainerTemplateConstructorParameterResolver(this.declarationContext, this.arguments,
				this.invocationIndex), //
			new ContainerTemplateInstanceFieldInjector(this.declarationContext, this.arguments, this.invocationIndex) //
		);
	}

	@Override
	public void prepareInvocation(ExtensionContext context) {
		super.prepareInvocation(context);
	}

}
