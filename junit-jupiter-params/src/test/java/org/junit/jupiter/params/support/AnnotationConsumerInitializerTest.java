/*
 * Copyright 2015-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.support.AnnotationConsumerInitializer.initialize;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.AnnotationBasedArgumentsProvider;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.platform.commons.JUnitException;

@DisplayName("AnnotationConsumerInitializer")
class AnnotationConsumerInitializerTest {

	@Test
	@DisplayName("should initialize annotation consumer")
	void shouldInitializeAnnotationConsumer() throws NoSuchMethodException {
		var instance = new SomeAnnotationConsumer();
		var method = SubjectClass.class.getDeclaredMethod("foo");
		var initialisedAnnotationConsumer = initialize(method, instance);

		assertThat(initialisedAnnotationConsumer.annotation) //
				.isInstanceOf(CsvSource.class) //
				.matches(annotation -> Arrays.equals(annotation.value(), new String[] { "a", "b" }));
	}

	@Test
	@DisplayName("should initialize annotation based arguments provider")
	void shouldInitializeAnnotationBasedArgumentsProvider() throws NoSuchMethodException {
		var instance = new SomeAnnotationBasedArgumentProvider();
		var method = SubjectClass.class.getDeclaredMethod("foo");
		var initialisedAnnotationConsumer = initialize(method, instance);

		initialisedAnnotationConsumer.provideArguments(mock());

		assertThat(initialisedAnnotationConsumer.assignedAnnotation) //
				.isInstanceOf(CsvSource.class) //
				.matches(annotation -> Arrays.equals(annotation.value(), new String[] { "a", "b" }));
	}

	@Test
	@DisplayName("should throw exception when method is not annotated")
	void shouldThrowExceptionWhenMethodIsNotAnnotated() throws NoSuchMethodException {
		var instance = new SomeAnnotationConsumer();
		var method = SubjectClass.class.getDeclaredMethod("noAnnotation");

		assertThatThrownBy(() -> initialize(method, instance)).isInstanceOf(JUnitException.class);
	}

	private static class SomeAnnotationBasedArgumentProvider extends AnnotationBasedArgumentsProvider<CsvSource> {

		CsvSource assignedAnnotation;

		@Override
		protected Stream<? extends Arguments> provideArguments(ExtensionContext context, CsvSource annotation) {
			assignedAnnotation = annotation;
			return Stream.empty();
		}
	}

	private static class SomeAnnotationConsumer implements AnnotationConsumer<CsvSource> {

		CsvSource annotation;

		@Override
		public void accept(CsvSource csvSource) {
			annotation = csvSource;
		}
	}

	private static class SubjectClass {

		@CsvSource({ "a", "b" })
		void foo() {

		}

		void noAnnotation() {

		}
	}
}
