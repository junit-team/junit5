/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine.extension;

import static org.junit.platform.commons.util.AnnotationUtils.findRepeatableAnnotations;
import static org.junit.platform.commons.util.AnnotationUtils.isAnnotated;

import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.TestFactoryExtension;

public class TestFactoryExtensionScanner {

	@SuppressWarnings("unchecked")
	public static Stream<Class<TestFactoryExtension>> streamTestFactoryExtensions(AnnotatedElement element) {
		// @formatter:off
		Stream<? extends Class<? extends Extension>> presentAnnotations =
				findRepeatableAnnotations(element, ExtendWith.class).stream()
						.map(ExtendWith::value)
						.flatMap(Arrays::stream)
						.filter(TestFactoryExtension.class::isAssignableFrom);
		// @formatter:on

		// @TestFactory can not be annotated with '@ExtendWith(TestFactoryAnnotationExtension.class)'
		// because that would create a dependency cycle between the api and the implementation modules.
		// For that reason, the extension has to be explicitly added here if the annotation is present.
		// @formatter:off
		Stream<Class<TestFactoryAnnotationExtension>> testFactoryAnnotationExtension =
				isAnnotated(element, TestFactory.class)
						? Stream.of(TestFactoryAnnotationExtension.class)
						: Stream.empty();
		// @formatter:on

		// the cast is save because both streams can only contain 'Class<? extends TestFactoryExtension>'s and
		// the types 'Stream' and 'Class' are covariant
		return (Stream<Class<TestFactoryExtension>>) (Stream) Stream.concat(presentAnnotations,
			testFactoryAnnotationExtension);
	}

}
