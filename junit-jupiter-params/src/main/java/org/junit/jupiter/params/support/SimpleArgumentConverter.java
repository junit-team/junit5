/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.params.support;

import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.ArgumentConversionException;
import org.junit.jupiter.params.ArgumentConverter;

public abstract class SimpleArgumentConverter implements ArgumentConverter {

	@Override
	public final Object convert(Object input, ParameterContext context) throws ArgumentConversionException {
		return convert(input, context.getParameter().getType());
	}

	protected abstract Object convert(Object input, Class<?> targetType);

}
