/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.execution.injection.sample;

import org.junit.gen5.api.extension.ExtensionContext;
import org.junit.gen5.api.extension.ParameterContext;
import org.junit.gen5.api.extension.ParameterResolver;
import org.junit.gen5.commons.util.ReflectionUtils;

/**
 * @since 5.0
 */
public class CustomAnnotationParameterResolver implements ParameterResolver {

	@Override
	public boolean supports(ParameterContext parameterContext, ExtensionContext extensionContext) {
		return parameterContext.getParameter().isAnnotationPresent(CustomAnnotation.class);
	}

	@Override
	public Object resolve(ParameterContext parameterContext, ExtensionContext extensionContext) {
		return ReflectionUtils.newInstance(parameterContext.getParameter().getType());
	}

}
