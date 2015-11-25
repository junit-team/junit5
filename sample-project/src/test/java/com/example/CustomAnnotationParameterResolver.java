/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.example;

import java.lang.reflect.Parameter;

import org.junit.gen5.api.extension.MethodParameterResolver;
import org.junit.gen5.api.extension.TestExecutionContext;
import org.junit.gen5.commons.util.AnnotationUtils;

/**
 * @since 5.0
 */
public class CustomAnnotationParameterResolver implements MethodParameterResolver {

	@Override
	public boolean supports(Parameter parameter, TestExecutionContext testExecutionContext) {
		return AnnotationUtils.isAnnotated(parameter, CustomAnnotation.class);
	}

}
