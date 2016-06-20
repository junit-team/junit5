/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine.execution.injection.sample;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;

/**
 * Example {@link ParameterResolver} that resolves primitive integers.
 *
 * @since 5.0
 */
public class PrimitiveIntegerParameterResolver implements ParameterResolver {

	@Override
	public boolean supports(ParameterContext parameterContext, ExtensionContext extensionContext) {
		return int.class == parameterContext.getParameter().getType();
	}

	@Override
	public Object resolve(ParameterContext parameterContext, ExtensionContext extensionContext) {
		return new Integer(42);
	}

}
