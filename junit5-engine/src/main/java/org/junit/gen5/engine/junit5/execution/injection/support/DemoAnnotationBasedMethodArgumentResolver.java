/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.execution.injection.support;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;

import org.junit.gen5.commons.util.AnnotationUtils;
import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.engine.junit5.execution.injection.MethodArgumentResolver;

/**
 * <strong>Only for demonstration purposes: will be removed at a later date.</strong>
 *
 * @since 5.0
 */
// TODO Move demo code to test source tree once extension mechanism is in place.
class DemoAnnotationBasedMethodArgumentResolver implements MethodArgumentResolver {

	private static final String ANNOTATION_NAME = "org.junit.gen5.engine.junit5.execution.injection.sample.CustomAnnotation";

	@Override
	public boolean supports(Parameter parameter) {
		// @formatter:off
		return ReflectionUtils.loadClass(ANNOTATION_NAME, Annotation.class)
				.map(clazz -> AnnotationUtils.findAnnotation(parameter, clazz).isPresent())
				.orElse(Boolean.FALSE)
				.booleanValue();
		// @formatter:on
	}

}
