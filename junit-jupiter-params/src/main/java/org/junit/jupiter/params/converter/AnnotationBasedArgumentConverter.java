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

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.lang.annotation.Annotation;

import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.support.AnnotationConsumer;
import org.junit.platform.commons.util.Preconditions;

/**
 * {@code AnnotationBasedArgumentConverter} is an abstract base class for
 * {@link ArgumentConverter} implementations that also need to consume an
 * annotation in order to perform the conversion.
 *
 * @since 5.10
 * @see ArgumentConverter
 * @see AnnotationConsumer
 * @see SimpleArgumentConverter
 */
@API(status = EXPERIMENTAL, since = "5.10")
public abstract class AnnotationBasedArgumentConverter<A extends Annotation>
		implements ArgumentConverter, AnnotationConsumer<A> {

	public AnnotationBasedArgumentConverter() {
	}

	private A annotation;

	@Override
	public final void accept(A annotation) {
		Preconditions.notNull(annotation, "annotation must not be null");
		this.annotation = annotation;
	}

	@Override
	public final Object convert(Object source, ParameterContext context) throws ArgumentConversionException {
		return convert(source, context.getParameter().getType(), this.annotation);
	}

	/**
	 * Convert the supplied {@code source} object into the supplied {@code targetType},
	 * based on metadata in the provided annotation.
	 *
	 * @param source the source object to convert; may be {@code null}
	 * @param targetType the target type the source object should be converted
	 * into; never {@code null}
	 * @param annotation the annotation to process; never {@code null}
	 * @return the converted object; may be {@code null} but only if the target
	 * type is a reference type
	 * @throws ArgumentConversionException in case an error occurs during the
	 * conversion
	 */
	protected abstract Object convert(Object source, Class<?> targetType, A annotation)
			throws ArgumentConversionException;

}
