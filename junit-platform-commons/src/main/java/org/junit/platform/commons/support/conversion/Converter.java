/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.commons.support.conversion;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import org.apiguardian.api.API;

/**
 * {@code Converter} is an abstraction that allows an input object to
 * be converted to an instance of a different class.
 *
 * <p>Implementations are loaded via the {@link java.util.ServiceLoader} and must
 * follow the service provider requirements. They should not make any assumptions
 * regarding when they are instantiated or how often they are called. Since
 * instances may potentially be cached and called from different threads, they
 * should be thread-safe.
 *
 * <p>Extend {@link TypedConverter} if your implementation always converts
 * from a given source type into a given target type and does not need access to
 * the {@link ClassLoader} to perform the conversion.
 *
 * @since 1.13
 * @see ConversionSupport
 * @see TypedConverter
 */
@API(status = EXPERIMENTAL, since = "1.13")
public interface Converter {

	/**
	 * Determine if the supplied source object can be converted into an instance
	 * of the specified target type.
	 *
	 * @param source the source object to convert; may be {@code null} but only
	 * if the target type is a reference type
	 * @param sourceType the descriptor of the source type; never {@code null}
	 * @param targetType the descriptor of the type the source should be converted into;
	 * never {@code null}
	 * @return {@code true} if the supplied source can be converted
	 */
	boolean canConvert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType);

	/**
	 * Convert the supplied source object into an instance of the specified
	 * target type.
	 * <p>This method will only be invoked if {@link #canConvert(Object, TypeDescriptor, TypeDescriptor)}
	 * returned {@code true} for the same target type.
	 *
	 * @param source the source object to convert; may be {@code null} but only
	 * if the target type is a reference type
	 * @param sourceType the descriptor of the source type; never {@code null}
	 * @param targetType the descriptor of the type the source should be converted into;
	 * never {@code null}
	 * @param classLoader the {@code ClassLoader} to use; never {@code null}
	 * @return the converted object; may be {@code null} but only if the target
	 * type is a reference type
	 * @throws ConversionException if an error occurs during the conversion
	 */
	Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType, ClassLoader classLoader)
			throws ConversionException;

}
