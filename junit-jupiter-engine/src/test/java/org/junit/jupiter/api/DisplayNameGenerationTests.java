/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import java.lang.reflect.Method;
import java.util.EmptyStackException;
import java.util.Stack;

import org.junit.jupiter.engine.AbstractJupiterTestEngineTests;
import org.junit.platform.engine.TestDescriptor;

/**
 * Check generated display names.
 *
 * @see DisplayName
 * @see DisplayNameGenerator
 * @see DisplayNameGeneration
 * @since 5.4
 */
class DisplayNameGenerationTests extends AbstractJupiterTestEngineTests {

	@Test
	void standardGenerator() {
		check(DefaultStyleTestCase.class, //
			"CONTAINER: DisplayNameGenerationTests$DefaultStyleTestCase", //
			"TEST: @DisplayName prevails", //
			"TEST: test()", //
			"TEST: test(TestInfo)", //
			"TEST: testUsingCamelCaseStyle()", //
			"TEST: testUsingCamelCase_and_also_UnderScores()", //
			"TEST: testUsingCamelCase_and_also_UnderScores_keepingParameterTypeNamesIntact(TestInfo)", //
			"TEST: test_with_underscores()" //
		);
	}

	@Test
	void simpleGenerator() {
		check(SimpleStyleTestCase.class, //
			"CONTAINER: DisplayNameGenerationTests$SimpleStyleTestCase", //
			"TEST: @DisplayName prevails", //
			"TEST: test", //
			"TEST: test (TestInfo)", //
			"TEST: testUsingCamelCaseStyle", //
			"TEST: testUsingCamelCase_and_also_UnderScores", //
			"TEST: testUsingCamelCase_and_also_UnderScores_keepingParameterTypeNamesIntact (TestInfo)", //
			"TEST: test_with_underscores" //
		);
	}

	@Test
	void underscoreGenerator() {
		var expectedDisplayNames = new String[] { //
				"<replace me>", //
				"TEST: @DisplayName prevails", //
				"TEST: test", //
				"TEST: test (TestInfo)", //
				"TEST: test with underscores", //
				"TEST: testUsingCamelCase and also UnderScores", //
				"TEST: testUsingCamelCase and also UnderScores keepingParameterTypeNamesIntact (TestInfo)", //
				"TEST: testUsingCamelCaseStyle" //
		};

		expectedDisplayNames[0] = "CONTAINER: DisplayNameGenerationTests$UnderscoreStyleTestCase";
		check(UnderscoreStyleTestCase.class, expectedDisplayNames);

		expectedDisplayNames[0] = "CONTAINER: DisplayNameGenerationTests$UnderscoreStyleInheritedFromSuperClassTestCase";
		check(UnderscoreStyleInheritedFromSuperClassTestCase.class, expectedDisplayNames);
	}

	@Test
	void indicativeSentencesGeneratorOnStaticNestedClass() {
		check(IndicativeStyleTestCase.class, //
			"CONTAINER: DisplayNameGenerationTests$IndicativeStyleTestCase", //
			"TEST: @DisplayName prevails", //
			"TEST: DisplayNameGenerationTests$IndicativeStyleTestCase -> test", //
			"TEST: DisplayNameGenerationTests$IndicativeStyleTestCase -> test (TestInfo)", //
			"TEST: DisplayNameGenerationTests$IndicativeStyleTestCase -> test with underscores", //
			"TEST: DisplayNameGenerationTests$IndicativeStyleTestCase -> testUsingCamelCase and also UnderScores", //
			"TEST: DisplayNameGenerationTests$IndicativeStyleTestCase -> testUsingCamelCase and also UnderScores keepingParameterTypeNamesIntact (TestInfo)", //
			"TEST: DisplayNameGenerationTests$IndicativeStyleTestCase -> testUsingCamelCaseStyle" //
		);
	}

	@Test
	void indicativeSentencesGeneratorOnTopLevelClass() {
		check(IndicativeSentencesTopLevelTestCase.class, //
			"CONTAINER: IndicativeSentencesTopLevelTestCase", //
			"CONTAINER: IndicativeSentencesTopLevelTestCase -> A year is a leap year", //
			"TEST: IndicativeSentencesTopLevelTestCase -> A year is a leap year -> if it is divisible by 4 but not by 100" //
		);
	}

	@Test
	void indicativeSentencesGeneratorOnNestedClass() {
		check(IndicativeSentencesNestedTestCase.class, //
			"CONTAINER: IndicativeSentencesNestedTestCase", //
			"CONTAINER: A year is a leap year", //
			"TEST: A year is a leap year -> if it is divisible by 4 but not by 100" //
		);
	}

	@Test
	void noNameGenerator() {
		check(NoNameStyleTestCase.class, //
			"CONTAINER: nn", //
			"TEST: @DisplayName prevails", //
			"TEST: nn", //
			"TEST: nn", //
			"TEST: nn", //
			"TEST: nn", //
			"TEST: nn", //
			"TEST: nn" //
		);
	}

	@Test
	void checkDisplayNameGeneratedForTestingAStackDemo() {
		check(StackTestCase.class, //
			"CONTAINER: A stack", //
			"TEST: is instantiated using its noarg constructor", //
			"CONTAINER: A new stack", //
			"TEST: throws an EmptyStackException when peeked", //
			"TEST: throws an EmptyStackException when popped", //
			"TEST: is empty", //
			"CONTAINER: After pushing an element to an empty stack", //
			"TEST: peek returns that element without removing it from the stack", //
			"TEST: pop returns that element and leaves an empty stack", //
			"TEST: the stack is no longer empty" //
		);
	}

	@Test
	void checkDisplayNameGeneratedForIndicativeGeneratorTestCase() {
		check(IndicativeGeneratorTestCase.class, //
			"CONTAINER: A stack", //
			"TEST: A stack, is instantiated with new constructor", //
			"CONTAINER: A stack, when new", //
			"TEST: A stack, when new, throws EmptyStackException when peeked", //
			"CONTAINER: A stack, when new, after pushing an element to an empty stack", //
			"TEST: A stack, when new, after pushing an element to an empty stack, is no longer empty" //
		);
	}

	@Test
	void checkDisplayNameGeneratedForIndicativeGeneratorWithCustomSeparatorTestCase() {
		check(IndicativeGeneratorWithCustomSeparatorTestCase.class, //
			"CONTAINER: A stack", //
			"TEST: A stack >> is instantiated with new constructor", //
			"CONTAINER: A stack >> when new", //
			"TEST: A stack >> when new >> throws EmptyStackException when peeked", //
			"CONTAINER: A stack >> when new >> after pushing an element to an empty stack", //
			"TEST: A stack >> when new >> after pushing an element to an empty stack >> is no longer empty" //
		);
	}

	@Test
	void displayNameGenerationInheritance() {
		check(DisplayNameGenerationInheritanceTestCase.InnerNestedTestCase.class, //
			"CONTAINER: DisplayNameGenerationInheritanceTestCase", //
			"CONTAINER: InnerNestedTestCase", //
			"TEST: this is a test"//
		);

		check(DisplayNameGenerationInheritanceTestCase.StaticNestedTestCase.class, //
			"CONTAINER: DisplayNameGenerationInheritanceTestCase$StaticNestedTestCase", //
			"TEST: this_is_a_test()"//
		);
	}

	@Test
	void indicativeSentencesGenerationInheritance() {
		check(IndicativeSentencesGenerationInheritanceTestCase.InnerNestedTestCase.class, //
			"CONTAINER: IndicativeSentencesGenerationInheritanceTestCase", //
			"CONTAINER: IndicativeSentencesGenerationInheritanceTestCase -> InnerNestedTestCase", //
			"TEST: IndicativeSentencesGenerationInheritanceTestCase -> InnerNestedTestCase -> this is a test"//
		);

		check(IndicativeSentencesGenerationInheritanceTestCase.StaticNestedTestCase.class, //
			"CONTAINER: IndicativeSentencesGenerationInheritanceTestCase$StaticNestedTestCase", //
			"TEST: this_is_a_test()"//
		);
	}

	private void check(Class<?> testClass, String... expectedDisplayNames) {
		var request = request().selectors(selectClass(testClass)).build();
		var descriptors = discoverTests(request).getDescendants();
		assertThat(descriptors).map(this::describe).containsExactlyInAnyOrder(expectedDisplayNames);
	}

	private String describe(TestDescriptor descriptor) {
		return descriptor.getType() + ": " + descriptor.getDisplayName();
	}

	// -------------------------------------------------------------------

	static class NoNameGenerator implements DisplayNameGenerator {

		@Override
		public String generateDisplayNameForClass(Class<?> testClass) {
			return "nn";
		}

		@Override
		public String generateDisplayNameForNestedClass(Class<?> nestedClass) {
			return "nn";
		}

		@Override
		public String generateDisplayNameForMethod(Class<?> testClass, Method testMethod) {
			return "nn";
		}
	}

	@DisplayNameGeneration(NoNameGenerator.class)
	static abstract class AbstractTestCase {
		@Test
		void test() {
		}

		@Test
		void test(TestInfo testInfo) {
			testInfo.getDisplayName();
		}

		@Test
		void testUsingCamelCaseStyle() {
		}

		@Test
		void testUsingCamelCase_and_also_UnderScores() {
		}

		@Test
		void testUsingCamelCase_and_also_UnderScores_keepingParameterTypeNamesIntact(TestInfo testInfo) {
			testInfo.getDisplayName();
		}

		@Test
		void test_with_underscores() {
		}

		@DisplayName("@DisplayName prevails")
		@Test
		void testDisplayNamePrevails() {
		}
	}

	@DisplayNameGeneration(DisplayNameGenerator.Standard.class)
	static class DefaultStyleTestCase extends AbstractTestCase {
	}

	@DisplayNameGeneration(DisplayNameGenerator.Simple.class)
	static class SimpleStyleTestCase extends AbstractTestCase {
	}

	@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
	static class UnderscoreStyleTestCase extends AbstractTestCase {
	}

	@IndicativeSentencesGeneration(separator = " -> ", generator = DisplayNameGenerator.ReplaceUnderscores.class)
	static class IndicativeStyleTestCase extends AbstractTestCase {
	}

	@DisplayNameGeneration(NoNameGenerator.class)
	static class NoNameStyleTestCase extends AbstractTestCase {
	}

	// No annotation here! @DisplayNameGeneration is inherited from super class
	static class UnderscoreStyleInheritedFromSuperClassTestCase extends UnderscoreStyleTestCase {
	}

	// -------------------------------------------------------------------

	@DisplayName("A stack")
	@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
	static class StackTestCase {

		Stack<Object> stack;

		@Test
		void is_instantiated_using_its_noarg_constructor() {
			new Stack<>();
		}

		@Nested
		class A_new_stack {

			@BeforeEach
			void createNewStack() {
				stack = new Stack<>();
			}

			@Test
			void is_empty() {
				assertTrue(stack.isEmpty());
			}

			@Test
			void throws_an_EmptyStackException_when_popped() {
				assertThrows(EmptyStackException.class, () -> stack.pop());
			}

			@Test
			void throws_an_EmptyStackException_when_peeked() {
				assertThrows(EmptyStackException.class, () -> stack.peek());
			}

			@Nested
			class After_pushing_an_element_to_an_empty_stack {

				String anElement = "an element";

				@BeforeEach
				void pushAnElement() {
					stack.push(anElement);
				}

				@Test
				void the_stack_is_no_longer_empty() {
					assertFalse(stack.isEmpty());
				}

				@Test
				void pop_returns_that_element_and_leaves_an_empty_stack() {
					assertEquals(anElement, stack.pop());
					assertTrue(stack.isEmpty());
				}

				@Test
				void peek_returns_that_element_without_removing_it_from_the_stack() {
					assertEquals(anElement, stack.peek());
					assertFalse(stack.isEmpty());
				}
			}
		}
	}

	// -------------------------------------------------------------------

	@DisplayName("A stack")
	@IndicativeSentencesGeneration(generator = DisplayNameGenerator.ReplaceUnderscores.class)
	static class IndicativeGeneratorTestCase {

		Stack<Object> stack;

		@Test
		void is_instantiated_with_new_constructor() {
			new Stack<>();
		}

		@Nested
		class when_new {

			@BeforeEach
			void create_with_new_stack() {
				stack = new Stack<>();
			}

			@Test
			void throws_EmptyStackException_when_peeked() {
				assertThrows(EmptyStackException.class, () -> stack.peek());
			}

			@Nested
			class after_pushing_an_element_to_an_empty_stack {

				String anElement = "an element";

				@BeforeEach
				void push_an_element() {
					stack.push(anElement);
				}

				@Test
				void is_no_longer_empty() {
					assertFalse(stack.isEmpty());
				}
			}
		}
	}

	// -------------------------------------------------------------------

	@DisplayName("A stack")
	@IndicativeSentencesGeneration(separator = " >> ", generator = DisplayNameGenerator.ReplaceUnderscores.class)
	static class IndicativeGeneratorWithCustomSeparatorTestCase {

		Stack<Object> stack;

		@Test
		void is_instantiated_with_new_constructor() {
			new Stack<>();
		}

		@Nested
		class when_new {

			@BeforeEach
			void create_with_new_stack() {
				stack = new Stack<>();
			}

			@Test
			void throws_EmptyStackException_when_peeked() {
				assertThrows(EmptyStackException.class, () -> stack.peek());
			}

			@Nested
			class after_pushing_an_element_to_an_empty_stack {

				String anElement = "an element";

				@BeforeEach
				void push_an_element() {
					stack.push(anElement);
				}

				@Test
				void is_no_longer_empty() {
					assertFalse(stack.isEmpty());
				}
			}
		}
	}
}
