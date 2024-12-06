
package org.junit.jupiter.params;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.TestExtensionContext.getExtensionContextReturningSingleMethod;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

public class ExpressionLanguageTests {

	private final ParameterizedTestExtension parameterizedTestExtension = new ParameterizedTestExtension();

	@Test
	void throwsExceptionWhenParameterizedTestIsNotInvokedAtLeastOnce() {
		var extensionContext = getExtensionContextReturningSingleMethod(new TestCaseWithAnnotatedMethod());
		this.parameterizedTestExtension.supportsTestTemplate(extensionContext);
		var stream = this.parameterizedTestExtension.provideTestTemplateInvocationContexts(extensionContext);
		assertThat(stream.findFirst().get().getDisplayName(0)).isEqualTo("hello!");
	}

	static class TestCaseWithAnnotatedMethod {

		@ExpressionLanguage(MustacheAdapter.class)
		@ParameterizedTest(name = "foo")
		@ArgumentsSource(FooArgumentsProvider.class)
		void method() {
		}

		static class Foo {
			String bar;

			public Foo(String bar) {
				this.bar = bar;
			}
		}

		static class FooArgumentsProvider implements ArgumentsProvider {

			@Override
			public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
				return Stream.of(arguments(new TestCaseWithAnnotatedMethod.Foo("123")),
					arguments(new TestCaseWithAnnotatedMethod.Foo("456")));
			}
		}
	}

	static class MustacheAdapter implements ExpressionLanguageAdapter {

		@Override
		public String evaluate(String template, Map<String, Object> context) {
			return "hello!";
		}
	}
}
