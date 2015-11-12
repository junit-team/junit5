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

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;

import org.junit.gen5.commons.util.AnnotationUtils;
import org.junit.gen5.commons.util.ReflectionUtils;

/**
 * @since 5.0
 */
class SimpleAnnotationBasedMethodArgumentResolver implements MethodArgumentResolver {

	private static final String ANNOTATION_NAME = "org.junit.gen5.engine.junit5.execution.injection.sample.CustomAnnotation";

	@Override
	public boolean supports(Parameter parameter) {
		// @formatter:off
		return ReflectionUtils.loadClass(ANNOTATION_NAME, Annotation.class)
				.map(clazz -> AnnotationUtils.findAnnotation(parameter, clazz).isPresent())
				.orElse(false);
		// @formatter:on
	}

}
