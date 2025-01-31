
package org.junit.jupiter.params;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.TestExtensionContext.getExtensionContextReturningSingleMethod;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.stream.Stream;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

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
		@ParameterizedTest(name = "foo {{name}}")
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

		MustacheFactory mustacheFactory;
		Mustache mustache;

		MustacheAdapter() {
			mustacheFactory = new DefaultMustacheFactory();
		}

		@Override
		public void compile(String template) {
			mustache = mustacheFactory.compile(new StringReader(template), template);
		}

		@Override
		public void format(ArgumentsContext argumentsContext, StringBuffer stringBuffer) {
			StringWriter stringWriter = new StringWriter();
			mustache.execute(stringWriter, argumentsContext);
			stringBuffer.append(stringWriter);
		}
	}
}
