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

import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.engine.junit5.execution.TestExecutionContext;

/**
 * @since 5.0
 */
public interface MethodArgumentResolver {

	// TODO Introduce custom MethodParameter type.
	boolean supports(Parameter parameter);

	// TODO Introduce custom MethodParameter type.
	default Object resolveArgument(Parameter parameter, TestExecutionContext testExecutionContext) {
		return ReflectionUtils.newInstance(parameter.getType());
	}

}
