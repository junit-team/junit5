/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.execution.injection;

import java.lang.reflect.Parameter;

// for a 'real' solution see: org.springframework.web.method.support.HandlerMethodArgumentResolver
public class SimpleTypeBasedMethodArgumentResolver implements MethodArgumentResolver {

	@Override
	public boolean supports(Parameter parameter) {
		Class<?> parameterType = parameter.getType();

		// TODO check should be based on class-objects not strings
		return (parameterType.getName().equals("org.junit.gen5.engine.junit5.execution.injection.sample.CustomType"));
	}

}
