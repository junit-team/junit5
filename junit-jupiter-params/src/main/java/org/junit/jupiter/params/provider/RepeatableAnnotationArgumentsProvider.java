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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.support.AnnotationConsumer;
import org.junit.platform.commons.util.Preconditions;

@API(status = EXPERIMENTAL, since = "5.11")
public abstract class RepeatableAnnotationArgumentsProvider<A extends Annotation>
		implements ArgumentsProvider, AnnotationConsumer<A> {

	public RepeatableAnnotationArgumentsProvider() {
	}

	private final List<A> annotations = new ArrayList<>();

	@Override
	public void accept(A annotation) {
		Preconditions.notNull(annotation, "annotation must not be null");
		annotations.add(annotation);
	}

	@Override
	public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
		return annotations.stream().flatMap(annotation -> provideArguments(context, annotation));
	}

	protected abstract Stream<? extends Arguments> provideArguments(ExtensionContext context, A annotation);
}
