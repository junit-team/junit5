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

import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;

import org.junit.gen5.commons.util.*;

// for a 'real' solution see: org.springframework.web.method.support.HandlerMethodArgumentResolver
public class SimpleAnnotationBasedMethodArgumentResolver implements MethodArgumentResolver {

	private final String annotationName = "org.junit.gen5.engine.junit5.execution.injection.sample.CustomAnnotation";

	@Override
	public boolean supports(Parameter parameter) {
		Optional<Class<Annotation>> classOptional = ReflectionUtils.loadClass(annotationName, Annotation.class);
		if (!classOptional.isPresent())
			return false;

		Class<Annotation> annotationClass = classOptional.get();
		List<Annotation> annotations = AnnotationUtils.findAllAnnotations(parameter, annotationClass);

		return !annotations.isEmpty();
	}

}
