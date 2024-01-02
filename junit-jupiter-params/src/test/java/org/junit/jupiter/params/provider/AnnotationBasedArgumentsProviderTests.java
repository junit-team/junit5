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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.MockCsvAnnotationBuilder.csvSource;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;

@DisplayName("AnnotationBasedArgumentsProvider")
class AnnotationBasedArgumentsProviderTests {

	private final AnnotationBasedArgumentsProvider<CsvSource> annotationBasedArgumentsProvider = new AnnotationBasedArgumentsProvider<>() {
		@Override
		protected Stream<? extends Arguments> provideArguments(ExtensionContext context, CsvSource annotation) {
			return Stream.empty();
		}
	};

	@Test
	@DisplayName("should throw exception when null annotation is provided to accept method")
	void shouldThrowExceptionWhenNullAnnotationIsProvidedToAccept() {
		assertThatThrownBy(() -> annotationBasedArgumentsProvider.accept(null)) //
				.hasMessage("annotation must not be null");
	}

	@Test
	@DisplayName("should invoke the provideArguments template method with the accepted annotation")
	void shouldInvokeTemplateMethodWithTheAnnotationProvidedToAccept() {
		var spiedProvider = spy(annotationBasedArgumentsProvider);
		var extensionContext = mock(ExtensionContext.class);
		var annotation = csvSource("0", "1", "2");

		annotationBasedArgumentsProvider.accept(annotation);
		annotationBasedArgumentsProvider.provideArguments(extensionContext);

		verify(spiedProvider, atMostOnce()).provideArguments(eq(extensionContext), eq(annotation));
	}

}
