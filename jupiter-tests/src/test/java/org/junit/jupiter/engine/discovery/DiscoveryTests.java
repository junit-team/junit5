/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.discovery;

import static java.util.Comparator.comparing;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.engine.discovery.JupiterUniqueIdBuilder.uniqueIdForTestTemplateMethod;
import static org.junit.platform.commons.util.CollectionUtils.getOnlyElement;
import static org.junit.platform.engine.discovery.ClassNameFilter.includeClassNamePatterns;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectNestedMethod;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectUniqueId;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.engine.AbstractJupiterTestEngineTests;
import org.junit.jupiter.engine.JupiterTestEngine;
import org.junit.jupiter.engine.descriptor.ClassTestDescriptor;
import org.junit.jupiter.engine.descriptor.NestedClassTestDescriptor;
import org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.engine.DiscoveryIssue;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.launcher.LauncherDiscoveryRequest;

/**
 * Test correct test discovery in simple test classes for the {@link JupiterTestEngine}.
 *
 * @since 5.0
 */
class DiscoveryTests extends AbstractJupiterTestEngineTests {

	@Test
	void discoverTestClass() {
		LauncherDiscoveryRequest request = request().selectors(selectClass(LocalTestCase.class)).build();
		TestDescriptor engineDescriptor = discoverTests(request).getEngineDescriptor();
		assertEquals(7, engineDescriptor.getDescendants().size(), "# resolved test descriptors");
	}

	@Test
	void doNotDiscoverAbstractTestClass() {
		LauncherDiscoveryRequest request = request().selectors(selectClass(AbstractTestCase.class)).build();
		TestDescriptor engineDescriptor = discoverTests(request).getEngineDescriptor();
		assertEquals(0, engineDescriptor.getDescendants().size(), "# resolved test descriptors");
	}

	@Test
	void discoverMethodByUniqueId() {
		LauncherDiscoveryRequest request = request().selectors(
			selectUniqueId(JupiterUniqueIdBuilder.uniqueIdForMethod(LocalTestCase.class, "test1()"))).build();
		TestDescriptor engineDescriptor = discoverTests(request).getEngineDescriptor();
		assertEquals(2, engineDescriptor.getDescendants().size(), "# resolved test descriptors");
	}

	@Test
	void discoverMethodByUniqueIdForOverloadedMethod() {
		LauncherDiscoveryRequest request = request().selectors(
			selectUniqueId(JupiterUniqueIdBuilder.uniqueIdForMethod(LocalTestCase.class, "test4()"))).build();
		TestDescriptor engineDescriptor = discoverTests(request).getEngineDescriptor();
		assertEquals(2, engineDescriptor.getDescendants().size(), "# resolved test descriptors");
	}

	@Test
	void discoverMethodByUniqueIdForOverloadedMethodVariantThatAcceptsArguments() {
		LauncherDiscoveryRequest request = request().selectors(selectUniqueId(JupiterUniqueIdBuilder.uniqueIdForMethod(
			LocalTestCase.class, "test4(" + TestInfo.class.getName() + ")"))).build();
		TestDescriptor engineDescriptor = discoverTests(request).getEngineDescriptor();
		assertEquals(2, engineDescriptor.getDescendants().size(), "# resolved test descriptors");
	}

	@Test
	void discoverMethodByMethodReference() throws NoSuchMethodException {
		Method testMethod = LocalTestCase.class.getDeclaredMethod("test3");

		LauncherDiscoveryRequest request = request().selectors(selectMethod(LocalTestCase.class, testMethod)).build();
		TestDescriptor engineDescriptor = discoverTests(request).getEngineDescriptor();
		assertEquals(2, engineDescriptor.getDescendants().size(), "# resolved test descriptors");
	}

	@Test
	void discoverMultipleMethodsOfSameClass() {
		LauncherDiscoveryRequest request = request().selectors(selectMethod(LocalTestCase.class, "test1"),
			selectMethod(LocalTestCase.class, "test2")).build();

		TestDescriptor engineDescriptor = discoverTests(request).getEngineDescriptor();

		assertThat(engineDescriptor.getChildren()).hasSize(1);
		TestDescriptor classDescriptor = getOnlyElement(engineDescriptor.getChildren());
		assertThat(classDescriptor.getChildren()).hasSize(2);
	}

	@Test
	void discoverCompositeSpec() {
		LauncherDiscoveryRequest spec = request().selectors(
			selectUniqueId(JupiterUniqueIdBuilder.uniqueIdForMethod(LocalTestCase.class, "test2()")),
			selectClass(LocalTestCase.class)).build();

		TestDescriptor engineDescriptor = discoverTests(spec).getEngineDescriptor();
		assertEquals(7, engineDescriptor.getDescendants().size(), "# resolved test descriptors");
	}

	@Test
	void discoverTestTemplateMethodByUniqueId() {
		LauncherDiscoveryRequest spec = request().selectors(
			selectUniqueId(uniqueIdForTestTemplateMethod(TestTemplateClass.class, "testTemplate()"))).build();

		TestDescriptor engineDescriptor = discoverTests(spec).getEngineDescriptor();
		assertEquals(2, engineDescriptor.getDescendants().size(), "# resolved test descriptors");
	}

	@Test
	void discoverTestTemplateMethodByMethodSelector() {
		LauncherDiscoveryRequest spec = request().selectors(
			selectMethod(TestTemplateClass.class, "testTemplate")).build();

		TestDescriptor engineDescriptor = discoverTests(spec).getEngineDescriptor();
		assertEquals(2, engineDescriptor.getDescendants().size(), "# resolved test descriptors");
	}

	@Test
	void discoverDeeplyNestedTestMethodByNestedMethodSelector() throws Exception {
		var selector = selectNestedMethod(
			List.of(TestCaseWithExtendedNested.class, TestCaseWithExtendedNested.ConcreteInner1.class),
			AbstractSuperClass.NestedInAbstractClass.class,
			AbstractSuperClass.NestedInAbstractClass.class.getDeclaredMethod("test"));
		LauncherDiscoveryRequest spec = request().selectors(selector).build();

		TestDescriptor engineDescriptor = discoverTests(spec).getEngineDescriptor();

		ClassTestDescriptor topLevelClassDescriptor = (ClassTestDescriptor) getOnlyElement(
			engineDescriptor.getChildren());
		assertThat(topLevelClassDescriptor.getTestClass()).isEqualTo(TestCaseWithExtendedNested.class);

		NestedClassTestDescriptor firstLevelNestedClassDescriptor = (NestedClassTestDescriptor) getOnlyElement(
			topLevelClassDescriptor.getChildren());
		assertThat(firstLevelNestedClassDescriptor.getTestClass()).isEqualTo(
			TestCaseWithExtendedNested.ConcreteInner1.class);

		NestedClassTestDescriptor secondLevelNestedClassDescriptor = (NestedClassTestDescriptor) getOnlyElement(
			firstLevelNestedClassDescriptor.getChildren());
		assertThat(secondLevelNestedClassDescriptor.getTestClass()).isEqualTo(
			AbstractSuperClass.NestedInAbstractClass.class);

		TestMethodTestDescriptor methodDescriptor = (TestMethodTestDescriptor) getOnlyElement(
			secondLevelNestedClassDescriptor.getChildren());
		assertThat(methodDescriptor.getTestMethod().getName()).isEqualTo("test");
	}

	@ParameterizedTest
	@MethodSource("requestsForTestClassWithInvalidTestMethod")
	void reportsWarningForTestClassWithInvalidTestMethod(LauncherDiscoveryRequest request) throws Exception {

		var method = InvalidTestMethodTestCase.class.getDeclaredMethod("test");

		var results = discoverTests(request);

		var discoveryIssues = results.getDiscoveryIssues().stream().sorted(comparing(DiscoveryIssue::message)).toList();
		assertThat(discoveryIssues).hasSize(3);
		assertThat(discoveryIssues.getFirst().message()) //
				.isEqualTo("@Test method '%s' must not be private. It will not be executed.", method.toGenericString());
		assertThat(discoveryIssues.get(1).message()) //
				.isEqualTo("@Test method '%s' must not be static. It will not be executed.", method.toGenericString());
		assertThat(discoveryIssues.getLast().message()) //
				.isEqualTo("@Test method '%s' must not return a value. It will not be executed.",
					method.toGenericString());
	}

	static List<Named<LauncherDiscoveryRequest>> requestsForTestClassWithInvalidTestMethod() {
		return List.of( //
			named("directly selected", request().selectors(selectClass(InvalidTestMethodTestCase.class)).build()), //
			named("indirectly selected", request() //
					.selectors(selectPackage(InvalidTestMethodTestCase.class.getPackageName())) //
					.filters(
						includeClassNamePatterns(Pattern.quote(InvalidTestMethodTestCase.class.getName()))).build()), //
			named("subclasses", request() //
					.selectors(selectClass(InvalidTestMethodSubclass1TestCase.class),
						selectClass(InvalidTestMethodSubclass2TestCase.class)) //
					.build()) //
		);
	}

	// -------------------------------------------------------------------

	@SuppressWarnings("unused")
	private static abstract class AbstractTestCase {

		@Test
		void abstractTest() {
		}
	}

	@SuppressWarnings("JUnitMalformedDeclaration")
	static class LocalTestCase {

		@Test
		void test1() {
		}

		@Test
		void test2() {
		}

		@Test
		void test3() {
		}

		@Test
		void test4() {
		}

		@Test
		void test4(TestInfo testInfo) {
		}

		@CustomTestAnnotation
		void customTestAnnotation() {
			/* no-op */
		}

	}

	@Test
	@Retention(RetentionPolicy.RUNTIME)
	@interface CustomTestAnnotation {
	}

	static class TestTemplateClass {

		@TestTemplate
		void testTemplate() {
		}

	}

	static abstract class AbstractSuperClass {
		@Nested
		class NestedInAbstractClass {
			@Test
			void test() {
			}
		}
	}

	static class TestCaseWithExtendedNested {
		@Nested
		class ConcreteInner1 extends AbstractSuperClass {
		}
	}

	@SuppressWarnings("JUnitMalformedDeclaration")
	static class InvalidTestMethodTestCase {
		@Test
		private static int test() {
			return fail("should not be called");
		}
	}

	static class InvalidTestMethodSubclass1TestCase extends InvalidTestMethodTestCase {
	}

	static class InvalidTestMethodSubclass2TestCase extends InvalidTestMethodTestCase {
	}

}
