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
import org.junit.jupiter.params.provider.Arguments;

class ParameterizedContainerInvocationContext extends ParameterizedInvocationContext<ParameterizedContainerClassContext>
		implements ContainerTemplateInvocationContext {

	ParameterizedContainerInvocationContext(ParameterizedInvocationNameFormatter formatter,
			ParameterizedContainerClassContext classContext, Arguments arguments, int invocationIndex) {
		super(formatter, classContext, arguments, invocationIndex);
	}

	@Override
	public List<Extension> getAdditionalExtensions() {
		// TODO #878 Register either parameter resolvers or field injectors?
		return Arrays.asList(
			new ParameterizedContainerParameterResolver(this.declarationContext, this.arguments, this.invocationIndex),
			new ParameterizedContainerFieldInjector(this.declarationContext, this.arguments, this.invocationIndex),
			createArgumentCountValidator());
	}
}
