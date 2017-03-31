/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.params.converter;

import static org.junit.platform.commons.meta.API.Usage.Experimental;

import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.platform.commons.meta.API;

/**
 * {@code SimpleArgumentConverter} is an abstract base class for {@link ArgumentConverter}
 * implementations.
 *
 * @since 5.0
 * @see ArgumentConverter
 */
@API(Experimental)
public abstract class SimpleArgumentConverter implements ArgumentConverter {

	@Override
	public final Object convert(Object source, ParameterContext context) throws ArgumentConversionException {
		return convert(source, context.getParameter().getType());
	}

	protected abstract Object convert(Object source, Class<?> targetType);

}
