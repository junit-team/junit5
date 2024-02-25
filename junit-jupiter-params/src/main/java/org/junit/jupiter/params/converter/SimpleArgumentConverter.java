/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params.converter;

import static org.apiguardian.api.API.Status.STABLE;

import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.ParameterContext;

/**
 * {@code SimpleArgumentConverter} is an abstract base class for
 * {@link ArgumentConverter} implementations that only need to know the target
 * type and do not need access to the {@link ParameterContext} to perform the
 * conversion.
 *
 * @since 5.0
 * @see ArgumentConverter
 * @see TypedArgumentConverter
 */
@API(status = STABLE, since = "5.7")
public abstract class SimpleArgumentConverter implements ArgumentConverter {

	public SimpleArgumentConverter() {
	}

	@Override
	public final Object convert(Object source, ParameterContext context) throws ArgumentConversionException {
		return convert(source, context.getParameter().getType());
	}

	/**
	 * Convert the supplied {@code source} object into the supplied
	 * {@code targetType}.
	 *
	 * @param source the source object to convert; may be {@code null}
	 * @param targetType the target type the source object should be converted
	 * into; never {@code null}
	 * @return the converted object; may be {@code null} but only if the target
	 * type is a reference type
	 * @throws ArgumentConversionException in case an error occurs during the
	 * conversion
	 */
	protected abstract Object convert(Object source, Class<?> targetType) throws ArgumentConversionException;

}
