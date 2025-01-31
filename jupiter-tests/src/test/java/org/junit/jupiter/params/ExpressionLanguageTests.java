
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
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

public class ExpressionLanguageTests {

	private final ParameterizedTestExtension parameterizedTestExtension = new ParameterizedTestExtension();

	@Test
	void throwsExceptionWhenParameterizedTestIsNotInvokedAtLeastOnce() {
		var extensionContext = getExtensionContextReturningSingleMethod(new TestCaseWithAnnotatedMethod());
		this.parameterizedTestExtension.supportsTestTemplate(extensionContext);
		var testTemplateInvocationContexts = this.parameterizedTestExtension.provideTestTemplateInvocationContexts(extensionContext).toList();
		assertThat(testTemplateInvocationContexts.get(0).getDisplayName(0)).isEqualTo("foo 123");
		assertThat(testTemplateInvocationContexts.get(1).getDisplayName(0)).isEqualTo("foo 456");
	}

	static class TestCaseWithAnnotatedMethod {

		@ExpressionLanguage(MustacheAdapter.class)
		@ParameterizedTest(name = "foo {{bar}}")
		@ArgumentsSource(FooArgumentsProvider.class)
		void method() {
		}

		static class Foo {
			String bar;
			String baz;

			public Foo(String bar, String baz) {
				this.bar = bar;
				this.baz = baz;
			}
		}

		static class FooArgumentsProvider implements ArgumentsProvider {

			@Override
			public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
				return Stream.of(arguments(new TestCaseWithAnnotatedMethod.Foo("123", "abc")),
					arguments(new TestCaseWithAnnotatedMethod.Foo("456", "def")));
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
		public void format(Object scope, StringBuffer result) {
			StringWriter stringWriter = new StringWriter();
			mustache.execute(stringWriter, scope);
			result.append(stringWriter);
		}
	}
}
