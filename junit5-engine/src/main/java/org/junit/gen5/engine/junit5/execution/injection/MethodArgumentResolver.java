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
import org.junit.gen5.engine.junit5.descriptor.*;

// for a 'real' solution see: org.springframework.web.method.support.HandlerMethodArgumentResolver
public class MethodArgumentResolver {

	public List<Object> prepareArguments(MethodTestDescriptor methodTestDescriptor)
			throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {

		Method testMethod = methodTestDescriptor.getTestMethod();

		List<Object> arguments = new ArrayList<>();

		if (testMethod.getParameterCount() > 0) {

			Parameter[] parameters = testMethod.getParameters();
			for (Parameter parameter : parameters) {
				Class<?> parameterType = parameter.getType();
				Annotation[] parameterAnnotations = parameter.getAnnotations();

				System.out.println("				parameterType = " + parameterType);
				System.out.println("				parameterAnnotations = " + Arrays.asList(parameterAnnotations));

				Object newInstance = ReflectionUtils.newInstance(parameterType);
				arguments.add(newInstance);
			}
		}

		return arguments;
	}

}
