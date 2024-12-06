package org.junit.jupiter.params;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.ParameterizedTestExtension.METHOD_CONTEXT_KEY;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExecutableInvoker;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstances;
import org.junit.jupiter.api.function.ThrowingConsumer;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.engine.execution.NamespaceAwareStore;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.engine.support.store.NamespacedHierarchicalStore;

public class ExpressionLanguageTests {

	private final ParameterizedTestExtension parameterizedTestExtension = new ParameterizedTestExtension();

	@Test
	void throwsExceptionWhenParameterizedTestIsNotInvokedAtLeastOnce() {
		var extensionContext = getExtensionContextReturningSingleMethod(new TestCaseWithAnnotatedMethod());
		this.parameterizedTestExtension.supportsTestTemplate(extensionContext);
		var stream = this.parameterizedTestExtension.provideTestTemplateInvocationContexts(extensionContext);
		assertThat(stream.findFirst().get().getDisplayName(0)).isEqualTo("hello!");
	}

	private ExtensionContext getExtensionContextReturningSingleMethod(Object testCase) {
		return getExtensionContextReturningSingleMethod(testCase, ignored -> Optional.empty());
	}

	private ExtensionContext getExtensionContextReturningSingleMethod(Object testCase,
			Function<String, Optional<String>> configurationSupplier) {

		var method = ReflectionUtils.findMethods(testCase.getClass(),
				it -> "method".equals(it.getName())).stream().findFirst();

		return new ExtensionContext() {

			private final NamespacedHierarchicalStore<Namespace> store = new NamespacedHierarchicalStore<>(null);

			@Override
			public Optional<Method> getTestMethod() {
				return method;
			}

			@Override
			public Optional<ExtensionContext> getParent() {
				return Optional.empty();
			}

			@Override
			public ExtensionContext getRoot() {
				return this;
			}

			@Override
			public String getUniqueId() {
				return null;
			}

			@Override
			public String getDisplayName() {
				return null;
			}

			@Override
			public Set<String> getTags() {
				return null;
			}

			@Override
			public Optional<AnnotatedElement> getElement() {
				return Optional.empty();
			}

			@Override
			public Optional<Class<?>> getTestClass() {
				return Optional.empty();
			}

			@Override
			public Optional<TestInstance.Lifecycle> getTestInstanceLifecycle() {
				return Optional.empty();
			}

			@Override
			public java.util.Optional<Object> getTestInstance() {
				return Optional.empty();
			}

			@Override
			public Optional<TestInstances> getTestInstances() {
				return Optional.empty();
			}

			@Override
			public Optional<Throwable> getExecutionException() {
				return Optional.empty();
			}

			@Override
			public Optional<String> getConfigurationParameter(String key) {
				return configurationSupplier.apply(key);
			}

			@Override
			public <T> Optional<T> getConfigurationParameter(String key, Function<String, T> transformer) {
				return configurationSupplier.apply(key).map(transformer);
			}

			@Override
			public void publishReportEntry(Map<String, String> map) {
			}

			@Override
			public void publishFile(String fileName, ThrowingConsumer<Path> action) {
			}

			@Override
			public Store getStore(Namespace namespace) {
				var store = new NamespaceAwareStore(this.store, namespace);
				method //
						.map(it -> new ParameterizedTestMethodContext(it, it.getAnnotation(ParameterizedTest.class), Optional.of(it.getAnnotation(ExpressionLanguage.class)))) //
						.ifPresent(ctx -> store.put(METHOD_CONTEXT_KEY, ctx));
				return store;
			}

			@Override
			public ExecutionMode getExecutionMode() {
				return ExecutionMode.SAME_THREAD;
			}

			@Override
			public ExecutableInvoker getExecutableInvoker() {
				return new ExecutableInvoker() {
					@Override
					public Object invoke(Method method, Object target) {
						return null;
					}

					@Override
					public <T> T invoke(Constructor<T> constructor, Object outerInstance) {
						return ReflectionUtils.newInstance(constructor);
					}
				};
			}
		};
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
				return Stream.of(arguments(new TestCaseWithAnnotatedMethod.Foo("123")), arguments(new TestCaseWithAnnotatedMethod.Foo("456")));
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
