/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.api.extension;

import java.lang.reflect.Parameter;

import org.junit.gen5.commons.util.ReflectionUtils;

/**
 * <p>Implementations must provide a no-args constructor.
 *
 * @author Matthias Merdes
 * @author Sam Brannen
 * @since 5.0
 */
public interface MethodParameterResolver extends TestExtension {

	boolean supports(Parameter parameter);

	default Object resolve(Parameter parameter, TestExecutionContext testExecutionContext)
			throws ParameterResolutionException {

		return ReflectionUtils.newInstance(parameter.getType());
	}

}
