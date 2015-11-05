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

import org.junit.gen5.commons.util.*;

// for a 'real' solution see: org.springframework.web.method.support.HandlerMethodArgumentResolver
public class SimpleAnnotationBasedMethodArgumentResolver implements MethodArgumentResolver {

	@Override
	public Object resolveArgumentForMethodParameter(Parameter parameter)
			throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
		Class<?> parameterType = parameter.getType();

		//todo: check should be based on class-objects not strings
		boolean match = false;
		for (Annotation parameterAnnotation : parameter.getAnnotations()) {
			if (parameterAnnotation.annotationType().getName().equals("com.example.CustomAnnotation"))
				match = true;
		}

		if (match)
			return ReflectionUtils.newInstance(parameterType);

		return null;
	}

}
