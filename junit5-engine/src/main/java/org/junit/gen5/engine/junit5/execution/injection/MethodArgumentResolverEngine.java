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

import static java.util.stream.Collectors.*;

import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;

import org.junit.gen5.engine.junit5.execution.TestExecutionContext;

/**
 * @author Sam Brannen
 * @since 5.0
 */
public class MethodArgumentResolverEngine {

	// TODO When introducing the extension mechanism this instance will have to come from
	// outside
	private final MethodArgumentResolverRegistry resolverRegistry = new PrimitiveMethodArgumentResolverRegistry();

	/**
	 * Resolve the list of arguments for the test method in the supplied
	 * {@link TestExecutionContext}.
	 *
	 * @param testExecutionContext the current test execution context
	 * @return the list of Objects to be used as arguments in the method
	 * invocation; an empty list in case of a no-arg method
	 * @throws ArgumentResolutionException
	 */
	public List<Object> resolveArguments(TestExecutionContext testExecutionContext) throws ArgumentResolutionException {
		// @formatter:off
		return Arrays.stream(testExecutionContext.getDescriptor().getTestMethod().getParameters())
				.map(param -> resolveArgument(param, testExecutionContext))
				.collect(toList());
		// @formatter:on
	}

	private Object resolveArgument(Parameter parameter, TestExecutionContext testExecutionContext) {
		try {
			// @formatter:off
			List<MethodArgumentResolver> matchingResolvers = this.resolverRegistry.getMethodArgumentResolvers().stream()
					.filter(resolver -> resolver.supports(parameter))
					.collect(toList());
			// @formatter:on

			if (matchingResolvers.size() == 0) {
				throw new ArgumentResolutionException(
					"No MethodArgumentResolver registered for parameter: " + parameter);
			}
			if (matchingResolvers.size() > 1) {
				// @formatter:off
				List<String> resolverNames = matchingResolvers.stream()
						.map(resolver -> resolver.getClass().getName())
						.collect(toList());
				// @formatter:on
				throw new ArgumentResolutionException(
					String.format("Discovered multiple competing MethodArgumentResolvers for parameter [%s]: %s",
						parameter, resolverNames));
			}
			return matchingResolvers.get(0).resolveArgument(parameter, testExecutionContext);
		}
		catch (Exception ex) {
			if (ex instanceof ArgumentResolutionException) {
				throw (ArgumentResolutionException) ex;
			}
			throw new ArgumentResolutionException(String.format("Failed to resolve parameter [%s] for method [%s]",
				parameter, testExecutionContext.getDescriptor().getTestMethod()), ex);
		}
	}

}
