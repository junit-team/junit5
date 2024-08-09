/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.discovery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.engine.discovery.JupiterUniqueIdBuilder.uniqueIdForTestTemplateMethod;
import static org.junit.platform.commons.util.CollectionUtils.getOnlyElement;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectNestedMethod;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectUniqueId;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.engine.AbstractJupiterTestEngineTests;
import org.junit.jupiter.engine.JupiterTestEngine;
import org.junit.jupiter.engine.descriptor.ClassTestDescriptor;
import org.junit.jupiter.engine.descriptor.NestedClassTestDescriptor;
import org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor;
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
		TestDescriptor engineDescriptor = discoverTests(request);
		assertEquals(7, engineDescriptor.getDescendants().size(), "# resolved test descriptors");
	}

	@Test
	void doNotDiscoverAbstractTestClass() {
		LauncherDiscoveryRequest request = request().selectors(selectClass(AbstractTestCase.class)).build();
		TestDescriptor engineDescriptor = discoverTests(request);
		assertEquals(0, engineDescriptor.getDescendants().size(), "# resolved test descriptors");
	}

	@Test
	void discoverMethodByUniqueId() {
		LauncherDiscoveryRequest request = request().selectors(
			selectUniqueId(JupiterUniqueIdBuilder.uniqueIdForMethod(LocalTestCase.class, "test1()"))).build();
		TestDescriptor engineDescriptor = discoverTests(request);
		assertEquals(2, engineDescriptor.getDescendants().size(), "# resolved test descriptors");
	}

	@Test
	void discoverMethodByUniqueIdForOverloadedMethod() {
		LauncherDiscoveryRequest request = request().selectors(
			selectUniqueId(JupiterUniqueIdBuilder.uniqueIdForMethod(LocalTestCase.class, "test4()"))).build();
		TestDescriptor engineDescriptor = discoverTests(request);
		assertEquals(2, engineDescriptor.getDescendants().size(), "# resolved test descriptors");
	}

	@Test
	void discoverMethodByUniqueIdForOverloadedMethodVariantThatAcceptsArguments() {
		LauncherDiscoveryRequest request = request().selectors(selectUniqueId(JupiterUniqueIdBuilder.uniqueIdForMethod(
			LocalTestCase.class, "test4(" + TestInfo.class.getName() + ")"))).build();
		TestDescriptor engineDescriptor = discoverTests(request);
		assertEquals(2, engineDescriptor.getDescendants().size(), "# resolved test descriptors");
	}

	@Test
	void discoverMethodByMethodReference() throws NoSuchMethodException {
		Method testMethod = LocalTestCase.class.getDeclaredMethod("test3");

		LauncherDiscoveryRequest request = request().selectors(selectMethod(LocalTestCase.class, testMethod)).build();
		TestDescriptor engineDescriptor = discoverTests(request);
		assertEquals(2, engineDescriptor.getDescendants().size(), "# resolved test descriptors");
	}

	@Test
	void discoverMultipleMethodsOfSameClass() {
		LauncherDiscoveryRequest request = request().selectors(selectMethod(LocalTestCase.class, "test1"),
			selectMethod(LocalTestCase.class, "test2")).build();

		TestDescriptor engineDescriptor = discoverTests(request);

		assertThat(engineDescriptor.getChildren()).hasSize(1);
		TestDescriptor classDescriptor = getOnlyElement(engineDescriptor.getChildren());
		assertThat(classDescriptor.getChildren()).hasSize(2);
	}

	@Test
	void discoverCompositeSpec() {
		LauncherDiscoveryRequest spec = request().selectors(
			selectUniqueId(JupiterUniqueIdBuilder.uniqueIdForMethod(LocalTestCase.class, "test2()")),
			selectClass(LocalTestCase.class)).build();

		TestDescriptor engineDescriptor = discoverTests(spec);
		assertEquals(7, engineDescriptor.getDescendants().size(), "# resolved test descriptors");
	}

	@Test
	void discoverTestTemplateMethodByUniqueId() {
		LauncherDiscoveryRequest spec = request().selectors(
			selectUniqueId(uniqueIdForTestTemplateMethod(TestTemplateClass.class, "testTemplate()"))).build();

		TestDescriptor engineDescriptor = discoverTests(spec);
		assertEquals(2, engineDescriptor.getDescendants().size(), "# resolved test descriptors");
	}

	@Test
	void discoverTestTemplateMethodByMethodSelector() {
		LauncherDiscoveryRequest spec = request().selectors(
			selectMethod(TestTemplateClass.class, "testTemplate")).build();

		TestDescriptor engineDescriptor = discoverTests(spec);
		assertEquals(2, engineDescriptor.getDescendants().size(), "# resolved test descriptors");
	}

	@Test
	void discoverDeeplyNestedTestMethodByNestedMethodSelector() throws Exception {
		var selector = selectNestedMethod(
			List.of(TestCaseWithExtendedNested.class, TestCaseWithExtendedNested.ConcreteInner1.class),
			AbstractSuperClass.NestedInAbstractClass.class,
			AbstractSuperClass.NestedInAbstractClass.class.getDeclaredMethod("test"));
		LauncherDiscoveryRequest spec = request().selectors(selector).build();

		TestDescriptor engineDescriptor = discoverTests(spec);

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

	// -------------------------------------------------------------------

	private static abstract class AbstractTestCase {

		@Test
		void abstractTest() {

		}
	}

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

}
