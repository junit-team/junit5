/*
 * Copyright 2015-2024 the original author or authors.
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
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.converter.AnnotationBasedArgumentConverter;
import org.junit.jupiter.params.converter.JavaTimeConversionPattern;
import org.junit.jupiter.params.provider.AnnotationBasedArgumentsProvider;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.platform.commons.JUnitException;

@DisplayName("AnnotationConsumerInitializer")
class AnnotationConsumerInitializerTests {

	@Test
	@DisplayName("should initialize annotation consumer")
	void shouldInitializeAnnotationConsumer() throws NoSuchMethodException {
		var instance = new SomeAnnotationConsumer();
		var method = SubjectClass.class.getDeclaredMethod("foo");
		var initialisedAnnotationConsumer = initialize(method, instance);

		assertThat(initialisedAnnotationConsumer.annotation) //
				.isInstanceOfSatisfying(CsvSource.class, //
					source -> assertThat(source.value()).containsExactly("a", "b"));
	}

	@Test
	@DisplayName("should initialize annotation-based ArgumentsProvider")
	void shouldInitializeAnnotationBasedArgumentsProvider() throws NoSuchMethodException {
		var instance = new SomeAnnotationBasedArgumentsProvider();
		var method = SubjectClass.class.getDeclaredMethod("foo");
		var initialisedAnnotationConsumer = initialize(method, instance);

		initialisedAnnotationConsumer.provideArguments(mock());

		assertThat(initialisedAnnotationConsumer.annotation) //
				.isInstanceOfSatisfying(CsvSource.class, //
					source -> assertThat(source.value()).containsExactly("a", "b"));
	}

	@Test
	@DisplayName("should initialize annotation-based ArgumentConverter")
	void shouldInitializeAnnotationBasedArgumentConverter() throws NoSuchMethodException {
		var instance = new SomeAnnotationBasedArgumentConverter();
		var parameter = SubjectClass.class.getDeclaredMethod("bar", LocalDate.class).getParameters()[0];
		var initialisedAnnotationConsumer = initialize(parameter, instance);

		ParameterContext parameterContext = mock();
		when(parameterContext.getParameter()).thenReturn(parameter);
		initialisedAnnotationConsumer.convert("source", parameterContext);

		assertThat(initialisedAnnotationConsumer.annotation) //
				.isInstanceOfSatisfying(JavaTimeConversionPattern.class, //
					annotation -> assertThat(annotation.value()).isEqualTo("pattern"));
	}

	@Test
	@DisplayName("should throw exception when method is not annotated")
	void shouldThrowExceptionWhenMethodIsNotAnnotated() throws NoSuchMethodException {
		var instance = new SomeAnnotationConsumer();
		var method = SubjectClass.class.getDeclaredMethod("noAnnotation", String.class);

		assertThatThrownBy(() -> initialize(method, instance)).isInstanceOf(JUnitException.class);
	}

	@Test
	@DisplayName("should throw exception when parameter is not annotated")
	void shouldThrowExceptionWhenParameterIsNotAnnotated() throws NoSuchMethodException {
		var instance = new SomeAnnotationConsumer();
		var parameter = SubjectClass.class.getDeclaredMethod("noAnnotation", String.class).getParameters()[0];

		assertThatThrownBy(() -> initialize(parameter, instance)).isInstanceOf(JUnitException.class);
	}

	private static class SomeAnnotationBasedArgumentsProvider extends AnnotationBasedArgumentsProvider<CsvSource> {

		CsvSource annotation;

		@Override
		protected Stream<? extends Arguments> provideArguments(ExtensionContext context, CsvSource annotation) {
			this.annotation = annotation;
			return Stream.empty();
		}
	}

	private static class SomeAnnotationBasedArgumentConverter
			extends AnnotationBasedArgumentConverter<JavaTimeConversionPattern> {

		JavaTimeConversionPattern annotation;

		@Override
		protected Object convert(Object source, Class<?> targetType, JavaTimeConversionPattern annotation) {
			this.annotation = annotation;
			return null;
		}
	}

	private static class SomeAnnotationConsumer implements AnnotationConsumer<CsvSource> {

		CsvSource annotation;

		@Override
		public void accept(CsvSource csvSource) {
			annotation = csvSource;
		}
	}

	@SuppressWarnings("unused")
	private static class SubjectClass {

		@CsvSource({ "a", "b" })
		void foo() {
		}

		void bar(@JavaTimeConversionPattern("pattern") LocalDate date) {
		}

		void noAnnotation(String param) {
		}
	}

}
