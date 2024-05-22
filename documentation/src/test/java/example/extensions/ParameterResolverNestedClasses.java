package example.extensions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.RegisterExtension;

// tag::user_guide[]
public class ParameterResolverNestedClasses {

	@Test
	@DisplayName("Outer class test")
	void testOuterClass(TestInfo testInfo) {
		assertEquals("Outer class test", testInfo.getDisplayName());
	}

	@Nested
	class FirstResolution {

		@RegisterExtension
		final FirstIntegerResolver firstIntegerResolver = new FirstIntegerResolver();

		@Test
		void testIntNested(int i) {
			assertEquals(1, i);
		}
	}

	@Nested
	class SecondResolution {

		@RegisterExtension
		final SecondIntegerResolver secondIntegerResolver = new SecondIntegerResolver();

		@Test
		void testIntNested(int i) {
			assertEquals(2, i);
		}
	}

	static class FirstIntegerResolver implements ParameterResolver {

		@Override
		public boolean supportsParameter(ParameterContext parameterContext,
				ExtensionContext extensionContext) {
			return parameterContext.getParameter().getType() == int.class;
		}

		@Override
		public Object resolveParameter(ParameterContext parameterContext,
				ExtensionContext extensionContext) {
			return 1;
		}
	}

	static class SecondIntegerResolver implements ParameterResolver {

		@Override
		public boolean supportsParameter(ParameterContext parameterContext,
				ExtensionContext extensionContext) {
			return parameterContext.getParameter().getType() == int.class;
		}

		@Override
		public Object resolveParameter(ParameterContext parameterContext,
				ExtensionContext extensionContext) {
			return 2;
		}
	}
}