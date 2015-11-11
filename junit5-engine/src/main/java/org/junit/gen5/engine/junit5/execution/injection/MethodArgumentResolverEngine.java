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

	//todo: when introducing the extension mechanism this instance will have to come from outside
	MethodArgumentResolverRegistry resolverRegistry = new PrimitiveMethodArgumentResolverRegistry();

	/**
	 * prepare a list of objects as arguments for the execution of this test method
	 *
	 * @param methodTestDescriptor the test descriptor for the underlying (test) method
	 * @return a list of Objects to be used as arguments in the method call - will be an empty list in case of no-arg methods
	 * @throws ArgumentResolutionException
	 */
	public List<Object> prepareArguments(MethodTestDescriptor methodTestDescriptor) throws ArgumentResolutionException {

		try {
			return this.doPrepareArguments(methodTestDescriptor);
		}
		catch (Exception root) {
			throw new ArgumentResolutionException("problem with argument resolution", root);
		}

	}

	private List<Object> doPrepareArguments(MethodTestDescriptor methodTestDescriptor)
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

		//todo: throw exception as well when there are more than 1 resolver for a given parameter
		for (MethodArgumentResolver argumentResolver : this.resolverRegistry.getMethodArgumentResolvers()) {
			if (argumentResolver.supports(parameter))
				return argumentResolver.resolveArgumentForMethodParameter(parameter);
		}

		throw new IllegalStateException("Error: no resolver found for parameter " + parameter);
	}

}
