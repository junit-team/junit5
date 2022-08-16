/*
 * Copyright 2015-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params.provider;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.lang.annotation.Annotation;
import java.util.stream.Stream;

import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.support.AnnotationConsumer;
import org.junit.platform.commons.util.Preconditions;

/**
 * The implementations of an {@code AnnotationBasedArgumentsProvider} are responsible
 * for {@linkplain #provideArguments providing} a stream of arguments to be passed to
 * a {@code @ParameterizedTest} method, by using the data given by an annotation.
 *
 * <p>An {@code ArgumentsProvider} can be registered via the
 * {@link ArgumentsSource @ArgumentsSource} annotation.
 *
 * @since 5.9
 * @see org.junit.jupiter.params.ParameterizedTest
 * @see org.junit.jupiter.params.provider.ArgumentsSource
 * @see org.junit.jupiter.params.provider.Arguments
 * @see org.junit.jupiter.params.support.AnnotationConsumer
 */
@API(status = EXPERIMENTAL, since = "5.9")
public abstract class AnnotationBasedArgumentsProvider<A extends Annotation>
		implements ArgumentsProvider, AnnotationConsumer<A> {

	private A annotation;

	/**
	 * @param annotation that provides the source of the parameters, never {@code null}
	 */
	@Override
	public final void accept(A annotation) {
		Preconditions.notNull(annotation, "annotation must not be null");
		this.annotation = annotation;
	}

	@Override
	public final Stream<? extends Arguments> provideArguments(ExtensionContext context) {
		Preconditions.notNull(context, "context must not be null");
		return provideArguments(context, annotation);
	}

	/**
	 * Provide a {@link Stream} of {@link Arguments} to be passed to a
	 * {@code @ParameterizedTest} method, by using the data given by an annotation.
	 *
	 * @param context the current extension context; never {@code null}
	 * @param annotation that provides the source of the parameters, never {@code null}
	 * @return a stream of arguments; never {@code null}
	 */
	protected abstract Stream<? extends Arguments> provideArguments(ExtensionContext context, A annotation);
}
