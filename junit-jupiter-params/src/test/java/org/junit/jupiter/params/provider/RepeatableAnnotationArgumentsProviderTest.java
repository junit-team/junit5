package org.junit.jupiter.params.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.MockCsvAnnotationBuilder.csvSource;
import static org.mockito.Mockito.mock;

import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.Spy;

class RepeatableAnnotationArgumentsProviderTest {

	@Spy
	private final RepeatableAnnotationArgumentsProvider<CsvSource> repeatableAnnotationArgumentsProvider = new RepeatableAnnotationArgumentsProvider<>() {

		@Override
		protected Stream<? extends Arguments> provideArguments(ExtensionContext context, CsvSource annotation) {
			return Stream.of(Arguments.of(annotation));
		}
	};

	@Test
	@DisplayName("should throw exception when null annotation is provided to accept method")
	void shouldThrowExceptionWhenNullAnnotationIsProvidedToAccept() {
		assertThatThrownBy(() -> repeatableAnnotationArgumentsProvider.accept(null)) //
				.hasMessage("annotation must not be null");
	}

	@Test
	@DisplayName("should invoke the provideArguments template method for every accepted annotation")
	void shouldInvokeTemplateMethodForEachAnnotationProvided() {
		var extensionContext = mock(ExtensionContext.class);
		var annotation1 = csvSource("1");
		var annotation2 = csvSource("2");

		repeatableAnnotationArgumentsProvider.accept(annotation1);
		repeatableAnnotationArgumentsProvider.accept(annotation2);

		var arguments = repeatableAnnotationArgumentsProvider.provideArguments(extensionContext).toList();

		assertThat(arguments).hasSize(2);
		assertThat(arguments.getFirst().get()[0]).isEqualTo(annotation1);
		assertThat(arguments.get(1).get()[0]).isEqualTo(annotation2);
	}

}