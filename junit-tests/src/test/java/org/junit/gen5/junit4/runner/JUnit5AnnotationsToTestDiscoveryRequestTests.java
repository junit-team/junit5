/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.junit4.runner;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.gen5.api.Assertions.assertEquals;
import static org.junit.gen5.api.Assertions.assertTrue;
import static org.junit.gen5.commons.util.CollectionUtils.getOnlyElement;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.gen5.api.Test;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestTag;
import org.junit.gen5.engine.discovery.ClassFilter;
import org.junit.gen5.engine.discovery.ClassSelector;
import org.junit.gen5.engine.discovery.PackageSelector;
import org.junit.gen5.engine.discovery.UniqueIdSelector;
import org.junit.gen5.launcher.EngineIdFilter;
import org.junit.gen5.launcher.Launcher;
import org.junit.gen5.launcher.PostDiscoveryFilter;
import org.junit.gen5.launcher.TestDiscoveryRequest;
import org.junit.gen5.launcher.TestPlan;
import org.junit.runners.model.InitializationError;
import org.mockito.ArgumentCaptor;

class JUnit5AnnotationsToTestDiscoveryRequestTests {

	@Test
	void requestsClassSelectorForAnnotatedClassWhenNoAdditionalAnnotationsArePresent() throws Exception {
		class TestClass {
		}

		TestDiscoveryRequest request = instantiateRunnerAndCaptureGeneratedRequest(TestClass.class);

		assertThat(request.getSelectors()).hasSize(1);
		ClassSelector classSelector = getOnlyElement(request.getSelectorsByType(ClassSelector.class));
		assertEquals(TestClass.class, classSelector.getTestClass());
	}

	@Test
	void requestsClassSelectorsWhenClassesAnnotationIsPresent() throws Exception {
		@Classes({ Short.class, Byte.class })
		class TestClass {
		}

		TestDiscoveryRequest request = instantiateRunnerAndCaptureGeneratedRequest(TestClass.class);

		assertThat(request.getSelectors()).hasSize(2);
		List<ClassSelector> selectors = request.getSelectorsByType(ClassSelector.class);
		assertEquals(Short.class, selectors.get(0).getTestClass());
		assertEquals(Byte.class, selectors.get(1).getTestClass());
	}

	@Test
	void requestsUniqueIdSelectorsWhenUniqueIdsAnnotationIsPresent() throws Exception {
		@UniqueIds({ "foo", "bar" })
		class TestClass {
		}

		TestDiscoveryRequest request = instantiateRunnerAndCaptureGeneratedRequest(TestClass.class);

		assertThat(request.getSelectors()).hasSize(2);
		List<UniqueIdSelector> selectors = request.getSelectorsByType(UniqueIdSelector.class);
		assertEquals("foo", selectors.get(0).getUniqueId());
		assertEquals("bar", selectors.get(1).getUniqueId());
	}

	@Test
	void requestsPackageSelectorsWhenPackagesAnnotationIsPresent() throws Exception {
		@Packages({ "foo", "bar" })
		class TestClass {
		}

		TestDiscoveryRequest request = instantiateRunnerAndCaptureGeneratedRequest(TestClass.class);

		assertThat(request.getSelectors()).hasSize(2);
		List<PackageSelector> selectors = request.getSelectorsByType(PackageSelector.class);
		assertEquals("foo", selectors.get(0).getPackageName());
		assertEquals("bar", selectors.get(1).getPackageName());
	}

	@Test
	void addsTagFilterToRequestWhenOnlyIncludeTagsAnnotationIsPresent() throws Exception {
		@OnlyIncludeTags({ "foo", "bar" })
		class TestClass {
		}

		TestDiscoveryRequest request = instantiateRunnerAndCaptureGeneratedRequest(TestClass.class);

		List<PostDiscoveryFilter> filters = request.getPostDiscoveryFilters();
		assertThat(filters).hasSize(1);

		PostDiscoveryFilter filter = filters.get(0);
		assertTrue(filter.filter(testDescriptorWithTag("foo")).included());
		assertTrue(filter.filter(testDescriptorWithTag("bar")).included());
		assertTrue(filter.filter(testDescriptorWithTag("baz")).excluded());
	}

	@Test
	void addsTagFilterToRequestWhenExcludeTagsAnnotationIsPresent() throws Exception {
		@ExcludeTags({ "foo", "bar" })
		class TestClass {
		}

		TestDiscoveryRequest request = instantiateRunnerAndCaptureGeneratedRequest(TestClass.class);

		List<PostDiscoveryFilter> filters = request.getPostDiscoveryFilters();
		assertThat(filters).hasSize(1);

		PostDiscoveryFilter filter = filters.get(0);
		assertTrue(filter.filter(testDescriptorWithTag("foo")).excluded());
		assertTrue(filter.filter(testDescriptorWithTag("bar")).excluded());
		assertTrue(filter.filter(testDescriptorWithTag("baz")).included());
	}

	@Test
	void addsEngineIdFilterToRequestWhenOnlyEngineAnnotationIsPresent() throws Exception {
		@OnlyEngine("foo")
		class TestClass {
		}

		TestDiscoveryRequest request = instantiateRunnerAndCaptureGeneratedRequest(TestClass.class);

		List<EngineIdFilter> filters = request.getEngineIdFilters();
		assertThat(filters).hasSize(1);

		EngineIdFilter filter = filters.get(0);
		assertTrue(filter.filter("foo").included());
		assertTrue(filter.filter("bar").excluded());
	}

	@Test
	void addsClassFilterToRequestWhenClassNamePatternAnnotationIsPresent() throws Exception {
		@ClassNamePattern(".*Foo")
		class TestClass {
		}
		class Foo {
		}
		class Bar {
		}

		TestDiscoveryRequest request = instantiateRunnerAndCaptureGeneratedRequest(TestClass.class);

		List<ClassFilter> filters = request.getDiscoveryFiltersByType(ClassFilter.class);
		assertThat(filters).hasSize(1);

		ClassFilter filter = filters.get(0);
		assertTrue(filter.filter(Foo.class).included());
		assertTrue(filter.filter(Bar.class).excluded());
	}

	private TestDescriptor testDescriptorWithTag(String tag) {
		TestDescriptor testDescriptor = mock(TestDescriptor.class);
		when(testDescriptor.getTags()).thenReturn(singleton(new TestTag(tag)));
		return testDescriptor;
	}

	private TestDiscoveryRequest instantiateRunnerAndCaptureGeneratedRequest(Class<?> testClass)
			throws InitializationError {
		Launcher launcher = mock(Launcher.class);
		ArgumentCaptor<TestDiscoveryRequest> captor = ArgumentCaptor.forClass(TestDiscoveryRequest.class);
		when(launcher.discover(captor.capture())).thenReturn(TestPlan.from(emptySet()));

		new JUnit5(testClass, launcher);

		return captor.getValue();
	}

}
