/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.theories.extensions;

import static org.apiguardian.api.API.Status.INTERNAL;

import java.util.Map;

import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.theories.domain.DataPointDetails;

/**
 * The parameter resolver that will be used to populate the arguments for
 * the theory parameters.
 */
@API(status = INTERNAL, since = "5.3")
public class TheoryParameterResolver implements ParameterResolver {
	private final Map<Integer, DataPointDetails> theoryArguments;

	/**
	 * Constructor.
	 *
	 * @param theoryArguments a map of parameter index to the
	 * corresponding argument
	 */
	public TheoryParameterResolver(Map<Integer, DataPointDetails> theoryArguments) {
		this.theoryArguments = theoryArguments;
	}

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {
		return theoryArguments.containsKey(parameterContext.getIndex());
	}

	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {
		DataPointDetails paramDataPointDetails = theoryArguments.get(parameterContext.getIndex());
		if (paramDataPointDetails == null) {
			throw new ParameterResolutionException("Unable to resolve argument for theory at index "
					+ parameterContext.getIndex() + " (" + parameterContext.getParameter().getName() + ")");
		}
		return paramDataPointDetails.getValue();
	}
}
