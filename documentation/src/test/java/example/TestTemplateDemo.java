/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package example;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;

class TestTemplateDemo {

	// tag::user_guide[]
	@TestTemplate
	@ExtendWith(MyTestTemplateInvocationContextProvider.class)
	void testTemplate(String parameter) {
		assertEquals(3, parameter.length());
	}

	// end::user_guide[]
	static
	// tag::user_guide[]
	public class MyTestTemplateInvocationContextProvider implements TestTemplateInvocationContextProvider {
		@Override
		public boolean supportsTestTemplate(ExtensionContext context) {
			return true;
		}

		@Override
		public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
			return Stream.of(invocationContext("foo"), invocationContext("bar"));
		}

		private TestTemplateInvocationContext invocationContext(String parameter) {
			return new TestTemplateInvocationContext() {
				@Override
				public String getDisplayName(int invocationIndex) {
					return parameter;
				}

				@Override
				public List<Extension> getAdditionalExtensions() {
					return Collections.singletonList(new ParameterResolver() {
						@Override
						public boolean supportsParameter(ParameterContext parameterContext,
								ExtensionContext extensionContext) {
							return parameterContext.getParameter().getType().equals(String.class);
						}

						@Override
						public Object resolveParameter(ParameterContext parameterContext,
								ExtensionContext extensionContext) {
							return parameter;
						}
					});
				}
			};
		}
	}
	// end::user_guide[]
}
