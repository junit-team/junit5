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

import java.lang.reflect.*;
import java.util.*;

import org.junit.gen5.engine.junit5.descriptor.*;

// for a 'real' solution see: org.springframework.web.method.support.HandlerMethodArgumentResolver
public class MethodArgumentResolverEngine {

	PrimitiveMethodArgumentResolverRegistry resolverRegistry = new PrimitiveMethodArgumentResolverRegistry();

	/**
	 * prepare a list of objects as arguments for the execution of this test method
	 *
	 * @param methodTestDescriptor the test descriptor for the underlying (test) method
	 * @return a list of Objects to be used as arguments in the method call - will be an empty list in case of no-arg methods
	 * @throws NoSuchMethodException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */

	public List<Object> prepareArguments(MethodTestDescriptor methodTestDescriptor)
			throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {

		Method testMethod = methodTestDescriptor.getTestMethod();

		List<Object> arguments = new ArrayList<>();

		if (testMethod.getParameterCount() > 0) {
			Parameter[] parameters = testMethod.getParameters();
			for (Parameter parameter : parameters) {
				Object newInstance = this.resolveArgumentForMethodParameter(parameter);
				arguments.add(newInstance);
			}
		}

		return arguments;
	}

	private Object resolveArgumentForMethodParameter(Parameter parameter)
			throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {

		for (MethodArgumentResolver argumentResolver : this.resolverRegistry.getMethodArgumentResolvers()) {
			if (argumentResolver.supports(parameter))
				return argumentResolver.resolveArgumentForMethodParameter(parameter);
		}

		throw new IllegalStateException("Error: no resolver found for parameter " + parameter);
	}

}
