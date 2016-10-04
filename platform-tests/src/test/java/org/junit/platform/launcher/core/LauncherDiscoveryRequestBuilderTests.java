/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.launcher.core;

import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.engine.FilterResult.excluded;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClasspathRoots;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectJavaClass;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectJavaMethod;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectJavaPackage;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectUniqueId;
import static org.junit.platform.launcher.EngineFilter.includeEngines;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.extensions.TempDirectory;
import org.junit.jupiter.extensions.TempDirectory.Root;
import org.junit.platform.commons.util.PreconditionViolationException;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.DiscoveryFilter;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.ClasspathRootSelector;
import org.junit.platform.engine.discovery.JavaClassSelector;
import org.junit.platform.engine.discovery.JavaMethodSelector;
import org.junit.platform.engine.discovery.JavaPackageSelector;
import org.junit.platform.engine.discovery.UniqueIdSelector;
import org.junit.platform.engine.test.TestEngineStub;
import org.junit.platform.launcher.DiscoveryFilterStub;
import org.junit.platform.launcher.EngineFilter;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.PostDiscoveryFilter;
import org.junit.platform.launcher.PostDiscoveryFilterStub;

/**
 * @since 1.0
 */
class LauncherDiscoveryRequestBuilderTests {

	@Nested
	class DiscoverySelectionTests {

		@Test
		public void packagesAreStoredInDiscoveryRequest() throws Exception {
			// @formatter:off
			LauncherDiscoveryRequest discoveryRequest = request()
					.selectors(
							selectJavaPackage("org.junit.platform.engine")
					).build();
			// @formatter:on

			List<String> packageSelectors = discoveryRequest.getSelectorsByType(JavaPackageSelector.class).stream().map(
				JavaPackageSelector::getPackageName).collect(toList());
			assertThat(packageSelectors).contains("org.junit.platform.engine");
		}

		@Test
		public void classesAreStoredInDiscoveryRequest() throws Exception {
			// @formatter:off
			LauncherDiscoveryRequest discoveryRequest = request()
					.selectors(
							selectJavaClass(LauncherDiscoveryRequestBuilderTests.class.getName()),
							selectJavaClass(SampleTestClass.class)
					)
				.build();
			// @formatter:on

			List<Class<?>> classes = discoveryRequest.getSelectorsByType(JavaClassSelector.class).stream().map(
				JavaClassSelector::getJavaClass).collect(toList());
			assertThat(classes).contains(SampleTestClass.class, LauncherDiscoveryRequestBuilderTests.class);
		}

		@Test
		public void methodsByFullyQualifiedNameAreStoredInDiscoveryRequest() throws Exception {
			// @formatter:off
			LauncherDiscoveryRequest discoveryRequest = request()
					.selectors(selectJavaMethod(fullyQualifiedMethodName()))
					.build();
			// @formatter:on

			List<JavaMethodSelector> methodSelectors = discoveryRequest.getSelectorsByType(JavaMethodSelector.class);
			assertThat(methodSelectors).hasSize(1);

			JavaMethodSelector methodSelector = methodSelectors.get(0);
			assertThat(methodSelector.getJavaClass()).isEqualTo(LauncherDiscoveryRequestBuilderTests.class);
			assertThat(methodSelector.getJavaMethod()).isEqualTo(fullyQualifiedMethod());
		}

		@Test
		public void methodsByNameAreStoredInDiscoveryRequest() throws Exception {
			Class<?> testClass = SampleTestClass.class;
			Method testMethod = testClass.getMethod("test");

			// @formatter:off
			LauncherDiscoveryRequest discoveryRequest = request()
					.selectors(selectJavaMethod(SampleTestClass.class.getName(), "test"))
					.build();
			// @formatter:on

			List<JavaMethodSelector> methodSelectors = discoveryRequest.getSelectorsByType(JavaMethodSelector.class);
			assertThat(methodSelectors).hasSize(1);

			JavaMethodSelector methodSelector = methodSelectors.get(0);
			assertThat(methodSelector.getJavaClass()).isEqualTo(testClass);
			assertThat(methodSelector.getJavaMethod()).isEqualTo(testMethod);
		}

		@Test
		public void methodsByClassAreStoredInDiscoveryRequest() throws Exception {
			Class<?> testClass = SampleTestClass.class;
			Method testMethod = testClass.getMethod("test");

			// @formatter:off
			DefaultDiscoveryRequest discoveryRequest = (DefaultDiscoveryRequest) request()
					.selectors(
							selectJavaMethod(SampleTestClass.class, "test")
					).build();
			// @formatter:on

			List<JavaMethodSelector> methodSelectors = discoveryRequest.getSelectorsByType(JavaMethodSelector.class);
			assertThat(methodSelectors).hasSize(1);

			JavaMethodSelector methodSelector = methodSelectors.get(0);
			assertThat(methodSelector.getJavaClass()).isEqualTo(testClass);
			assertThat(methodSelector.getJavaMethod()).isEqualTo(testMethod);
		}

		@Test
		public void unavailableFoldersAreNotStoredInDiscoveryRequest() throws Exception {
			// @formatter:off
			LauncherDiscoveryRequest discoveryRequest = request()
					.selectors(
							selectClasspathRoots(singleton(Paths.get("some", "local", "path")))
					).build();
			// @formatter:on

			assertThat(discoveryRequest.getSelectorsByType(ClasspathRootSelector.class).size()).isEqualTo(0);
		}

		@Test
		@ExtendWith(TempDirectory.class)
		public void availableFoldersAreStoredInDiscoveryRequest(@Root Path tempDir) throws Exception {
			// @formatter:off
			LauncherDiscoveryRequest discoveryRequest = request()
					.selectors(
							selectClasspathRoots(singleton(tempDir))
					).build();
			// @formatter:on

			List<Path> folders = discoveryRequest.getSelectorsByType(ClasspathRootSelector.class).stream().map(
				ClasspathRootSelector::getClasspathRoot).map(Paths::get).collect(toList());

			assertThat(folders).contains(tempDir);
		}

		@Test
		public void uniqueIdsAreStoredInDiscoveryRequest() throws Exception {
			UniqueId id1 = UniqueId.forEngine("engine").append("foo", "id1");
			UniqueId id2 = UniqueId.forEngine("engine").append("foo", "id2");

			// @formatter:off
			LauncherDiscoveryRequest discoveryRequest = request()
					.selectors(
							selectUniqueId(id1),
							selectUniqueId(id2)
					).build();
			// @formatter:on

			List<String> uniqueIds = discoveryRequest.getSelectorsByType(UniqueIdSelector.class).stream().map(
				UniqueIdSelector::getUniqueId).map(Object::toString).collect(toList());

			assertThat(uniqueIds).contains(id1.toString(), id2.toString());
		}
	}

	@Nested
	class DiscoveryFilterTests {

		@Test
		public void engineFiltersAreStoredInDiscoveryRequest() throws Exception {

			TestEngine engine1 = new TestEngineStub("engine1");
			TestEngine engine2 = new TestEngineStub("engine2");
			TestEngine engine3 = new TestEngineStub("engine3");

			// @formatter:off
			LauncherDiscoveryRequest discoveryRequest = request()
					.filters(includeEngines(engine1.getId(), engine2.getId()))
					.build();
			// @formatter:on

			List<EngineFilter> filters = discoveryRequest.getEngineFilters();
			assertThat(filters).hasSize(1);
			EngineFilter engineFilter = filters.get(0);
			assertTrue(engineFilter.apply(engine1).included());
			assertTrue(engineFilter.apply(engine2).included());
			assertTrue(engineFilter.apply(engine3).excluded());
		}

		@Test
		@SuppressWarnings("rawtypes")
		public void discoveryFiltersAreStoredInDiscoveryRequest() throws Exception {

			// @formatter:off
			LauncherDiscoveryRequest discoveryRequest = request()
					.filters(
							new DiscoveryFilterStub("filter1"),
							new DiscoveryFilterStub("filter2")
					).build();
			// @formatter:on

			List<String> filterStrings = discoveryRequest.getDiscoveryFiltersByType(DiscoveryFilter.class).stream().map(
				DiscoveryFilter::toString).collect(toList());
			assertThat(filterStrings).hasSize(2);
			assertThat(filterStrings).contains("filter1", "filter2");
		}

		@Test
		public void postDiscoveryFiltersAreStoredInDiscoveryRequest() throws Exception {

			// @formatter:off
			LauncherDiscoveryRequest discoveryRequest = request()
					.filters(
							new PostDiscoveryFilterStub("postFilter1"),
							new PostDiscoveryFilterStub("postFilter2")
					).build();
			// @formatter:on

			List<String> filterStrings = discoveryRequest.getPostDiscoveryFilters().stream().map(
				PostDiscoveryFilter::toString).collect(toList());
			assertThat(filterStrings).hasSize(2);
			assertThat(filterStrings).contains("postFilter1", "postFilter2");
		}

		@Test
		public void exceptionForIllegalFilterClass() throws Exception {
			Exception exception = assertThrows(PreconditionViolationException.class,
				() -> request().filters(o -> excluded("reason")));

			assertThat(exception).hasMessageStartingWith("Filter");
			assertThat(exception).hasMessageEndingWith(
				"must implement EngineFilter, PostDiscoveryFilter, or DiscoveryFilter.");
		}
	}

	@Nested
	class DiscoveryConfigurationParameterTests {

		@Test
		void withoutConfigurationParametersSet_NoConfigurationParametersAreStoredInDiscoveryRequest() throws Exception {
			LauncherDiscoveryRequest discoveryRequest = request().build();

			ConfigurationParameters configurationParameters = discoveryRequest.getConfigurationParameters();
			assertThat(configurationParameters.get("key").isPresent()).isFalse();
		}

		@Test
		void configurationParameterAddedDirectly_isStoredInDiscoveryRequest() throws Exception {
			// @formatter:off
			LauncherDiscoveryRequest discoveryRequest = request()
					.configurationParameter("key", "value")
					.build();
			// @formatter:on

			ConfigurationParameters configurationParameters = discoveryRequest.getConfigurationParameters();
			assertThat(configurationParameters.get("key").isPresent()).isTrue();
			assertThat(configurationParameters.get("key").get()).isEqualTo("value");
		}

		@Test
		void configurationParameterAddedDirectlyTwice_overridesPreviousValueInDiscoveryRequest() throws Exception {
			// @formatter:off
			LauncherDiscoveryRequest discoveryRequest = request()
					.configurationParameter("key", "value")
					.configurationParameter("key", "value-new")
					.build();
			// @formatter:on

			ConfigurationParameters configurationParameters = discoveryRequest.getConfigurationParameters();
			assertThat(configurationParameters.get("key").isPresent()).isTrue();
			assertThat(configurationParameters.get("key").get()).isEqualTo("value-new");
		}

		@Test
		void multipleConfigurationParameterAddedDirectly_isStoredInDiscoveryRequest() throws Exception {
			// @formatter:off
			LauncherDiscoveryRequest discoveryRequest = request()
					.configurationParameter("key1", "value1")
					.configurationParameter("key2", "value2")
					.build();
			// @formatter:on

			ConfigurationParameters configurationParameters = discoveryRequest.getConfigurationParameters();
			assertThat(configurationParameters.get("key1").isPresent()).isTrue();
			assertThat(configurationParameters.get("key1").get()).isEqualTo("value1");
			assertThat(configurationParameters.get("key2").isPresent()).isTrue();
			assertThat(configurationParameters.get("key2").get()).isEqualTo("value2");
		}

		@Test
		void configurationParameterAddedByMap_isStoredInDiscoveryRequest() throws Exception {
			HashMap<String, String> configurationParams = new HashMap<>();
			configurationParams.put("key", "value");

			// @formatter:off
			LauncherDiscoveryRequest discoveryRequest = request()
					.configurationParameters(configurationParams)
					.build();
			// @formatter:on

			ConfigurationParameters configurationParameters = discoveryRequest.getConfigurationParameters();
			assertThat(configurationParameters.get("key").isPresent()).isTrue();
			assertThat(configurationParameters.get("key").get()).isEqualTo("value");
		}

		@Test
		void multipleConfigurationParametersAddedByMap_areStoredInDiscoveryRequest() throws Exception {
			HashMap<String, String> configurationParams = new HashMap<>();
			configurationParams.put("key1", "value1");
			configurationParams.put("key2", "value2");

			// @formatter:off
			LauncherDiscoveryRequest discoveryRequest = request()
					.configurationParameters(configurationParams)
					.build();
			// @formatter:on

			ConfigurationParameters configurationParameters = discoveryRequest.getConfigurationParameters();
			assertThat(configurationParameters.get("key1").isPresent()).isTrue();
			assertThat(configurationParameters.get("key1").get()).isEqualTo("value1");
			assertThat(configurationParameters.get("key2").isPresent()).isTrue();
			assertThat(configurationParameters.get("key2").get()).isEqualTo("value2");
		}
	}

	private static class SampleTestClass {

		@Test
		public void test() {
		}
	}

	private static String fullyQualifiedMethodName() {
		return LauncherDiscoveryRequestBuilderTests.class.getName() + "#" + fullyQualifiedMethod().getName();
	}

	private static Method fullyQualifiedMethod() {
		try {
			return LauncherDiscoveryRequestBuilderTests.class.getDeclaredMethod("myTest");
		}
		catch (Exception ex) {
			throw new IllegalStateException(ex);
		}
	}

	void myTest() {
	}

}
