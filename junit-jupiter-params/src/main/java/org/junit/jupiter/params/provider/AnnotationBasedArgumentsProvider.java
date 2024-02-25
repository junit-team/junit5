/*
 * Copyright 2015-2024 the original author or authors.
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
 * {@code AnnotationBasedArgumentsProvider} is an abstract base class for
 * {@link ArgumentsProvider} implementations that also need to consume an
 * annotation in order to provide the arguments.
 *
 * @since 5.10
 * @see org.junit.jupiter.params.ParameterizedTest
 * @see org.junit.jupiter.params.provider.ArgumentsSource
 * @see org.junit.jupiter.params.provider.Arguments
 * @see org.junit.jupiter.params.provider.ArgumentsProvider
 * @see org.junit.jupiter.params.support.AnnotationConsumer
 */
@API(status = EXPERIMENTAL, since = "5.10")
public abstract class AnnotationBasedArgumentsProvider<A extends Annotation>
		implements ArgumentsProvider, AnnotationConsumer<A> {

	public AnnotationBasedArgumentsProvider() {
	}

	private A annotation;

	@Override
	public final void accept(A annotation) {
		Preconditions.notNull(annotation, "annotation must not be null");
		this.annotation = annotation;
	}

	@Override
	public final Stream<? extends Arguments> provideArguments(ExtensionContext context) {
		return provideArguments(context, this.annotation);
	}

	/**
	 * Provide a {@link Stream} of {@link Arguments} &mdash; based on metadata in the
	 * provided annotation &mdash; to be passed to a {@code @ParameterizedTest} method.
	 *
	 * @param context the current extension context; never {@code null}
	 * @param annotation the annotation to process; never {@code null}
	 * @return a stream of arguments; never {@code null}
	 */
	protected abstract Stream<? extends Arguments> provideArguments(ExtensionContext context, A annotation);

}
