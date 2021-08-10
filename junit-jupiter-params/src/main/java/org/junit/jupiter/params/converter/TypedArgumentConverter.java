/*
 * Copyright 2015-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params.converter;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.platform.commons.util.Preconditions;

/**
 * {@code TypedArgumentConverter} is an {@code ArgumentConverter} that always
 * converts objects of a given source type into a given target type.
 *
 * @param <S> the type of the source argument to convert
 * @param <T> the type of the target object to create from the source
 * @since 5.7
 * @see ArgumentConverter
 */
@API(status = EXPERIMENTAL, since = "5.7")
public abstract class TypedArgumentConverter<S, T> implements ArgumentConverter {

	private final Class<S> sourceType;
	private final Class<T> targetType;

	/**
	 * Create a new {@codeTypedArgumentConverter}.
	 *
	 * @param sourceType the type of the argument to convert; never {@code null}
	 * @param targetType the type of the target object to create from the source;
	 * never {@code null}
	 */
	protected TypedArgumentConverter(Class<S> sourceType, Class<T> targetType) {
		this.sourceType = Preconditions.notNull(sourceType, "sourceType must not be null");
		this.targetType = Preconditions.notNull(targetType, "targetType must not be null");
	}

	@Override
	public final Object convert(Object source, ParameterContext context) throws ArgumentConversionException {
		if (source == null) {
			return convert(null);
		}
		if (!this.sourceType.isAssignableFrom(source.getClass())) {
			throw new ArgumentConversionException("Can only convert objects of type " + this.sourceType.getName());
		}
		if (!this.targetType.isAssignableFrom(context.getParameter().getType())) {
			throw new ArgumentConversionException("Can only convert to type " + this.targetType.getName());
		}
		return convert(this.sourceType.cast(source));
	}

	/**
	 * Convert the supplied {@code source} object of type {@code S} into an object
	 * of type {@code T}.
	 *
	 * @param source the source object to convert; may be {@code null}
	 * @return the converted object
	 * @throws ArgumentConversionException in case an error occurs during the
	 * conversion
	 */
	protected abstract T convert(S source) throws ArgumentConversionException;

}
