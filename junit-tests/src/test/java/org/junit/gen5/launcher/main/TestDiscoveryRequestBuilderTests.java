/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.launcher.main;

import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.gen5.api.Assertions.assertTrue;
import static org.junit.gen5.api.Assertions.expectThrows;
import static org.junit.gen5.engine.FilterResult.excluded;
import static org.junit.gen5.engine.discovery.ClassSelector.selectClass;
import static org.junit.gen5.engine.discovery.ClasspathSelector.selectClasspathRoots;
import static org.junit.gen5.engine.discovery.MethodSelector.selectMethod;
import static org.junit.gen5.engine.discovery.PackageSelector.selectPackage;
import static org.junit.gen5.engine.discovery.UniqueIdSelector.selectUniqueId;
import static org.junit.gen5.launcher.EngineFilter.includeEngines;
import static org.junit.gen5.launcher.main.TestDiscoveryRequestBuilder.request;

import java.io.File;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

import org.assertj.core.util.Files;
import org.junit.gen5.api.Nested;
import org.junit.gen5.api.Test;
import org.junit.gen5.commons.util.PreconditionViolationException;
import org.junit.gen5.engine.ConfigurationParameters;
import org.junit.gen5.engine.DiscoveryFilter;
import org.junit.gen5.engine.TestEngine;
import org.junit.gen5.engine.UniqueId;
import org.junit.gen5.engine.discovery.ClassSelector;
import org.junit.gen5.engine.discovery.ClasspathSelector;
import org.junit.gen5.engine.discovery.MethodSelector;
import org.junit.gen5.engine.discovery.PackageSelector;
import org.junit.gen5.engine.discovery.UniqueIdSelector;
import org.junit.gen5.engine.junit5.stubs.TestEngineStub;
import org.junit.gen5.launcher.DiscoveryFilterStub;
import org.junit.gen5.launcher.EngineFilter;
import org.junit.gen5.launcher.PostDiscoveryFilter;
import org.junit.gen5.launcher.PostDiscoveryFilterStub;
import org.junit.gen5.launcher.TestDiscoveryRequest;

/**
 * @since 5.0
 */
public class TestDiscoveryRequestBuilderTests {

	@Nested
	class DiscoverySelectionTests {

		@Test
		public void packagesAreStoredInDiscoveryRequest() throws Exception {
			// @formatter:off
			TestDiscoveryRequest discoveryRequest = request()
					.selectors(
							selectPackage("org.junit.gen5.engine")
					).build();
			// @formatter:on

			List<String> packageSelectors = discoveryRequest.getSelectorsByType(PackageSelector.class).stream().map(
				PackageSelector::getPackageName).collect(toList());
			assertThat(packageSelectors).contains("org.junit.gen5.engine");
		}

		@Test
		public void classesAreStoredInDiscoveryRequest() throws Exception {
			// @formatter:off
			TestDiscoveryRequest discoveryRequest = request()
					.selectors(
							selectClass(TestDiscoveryRequestBuilderTests.class.getName()),
							selectClass(SampleTestClass.class)
					)
				.build();
			// @formatter:on

			List<Class<?>> classes = discoveryRequest.getSelectorsByType(ClassSelector.class).stream().map(
				ClassSelector::getJavaClass).collect(toList());
			assertThat(classes).contains(SampleTestClass.class, TestDiscoveryRequestBuilderTests.class);
		}

		@Test
		public void methodsByNameAreStoredInDiscoveryRequest() throws Exception {
			Class<?> testClass = SampleTestClass.class;
			Method testMethod = testClass.getMethod("test");

			// @formatter:off
			TestDiscoveryRequest discoveryRequest = request()
					.selectors(selectMethod(SampleTestClass.class.getName(), "test"))
					.build();
			// @formatter:on

			List<MethodSelector> methodSelectors = discoveryRequest.getSelectorsByType(MethodSelector.class);
			assertThat(methodSelectors).hasSize(1);

			MethodSelector methodSelector = methodSelectors.get(0);
			assertThat(methodSelector.getJavaClass()).isEqualTo(testClass);
			assertThat(methodSelector.getJavaMethod()).isEqualTo(testMethod);
		}

		@Test
		public void methodsByClassAreStoredInDiscoveryRequest() throws Exception {
			Class<?> testClass = SampleTestClass.class;
			Method testMethod = testClass.getMethod("test");

			// @formatter:off
			DiscoveryRequest discoveryRequest = (DiscoveryRequest) request()
					.selectors(
							MethodSelector.selectMethod(SampleTestClass.class, "test")
					).build();
			// @formatter:on

			List<MethodSelector> methodSelectors = discoveryRequest.getSelectorsByType(MethodSelector.class);
			assertThat(methodSelectors).hasSize(1);

			MethodSelector methodSelector = methodSelectors.get(0);
			assertThat(methodSelector.getJavaClass()).isEqualTo(testClass);
			assertThat(methodSelector.getJavaMethod()).isEqualTo(testMethod);
		}

		@Test
		public void unavailableFoldersAreNotStoredInDiscoveryRequest() throws Exception {
			// @formatter:off
			TestDiscoveryRequest discoveryRequest = request()
					.selectors(
							selectClasspathRoots(singleton(new File("/some/local/path")))
					).build();
			// @formatter:on

			assertThat(discoveryRequest.getSelectorsByType(ClasspathSelector.class).size()).isEqualTo(0);
		}

		@Test
		public void availableFoldersAreStoredInDiscoveryRequest() throws Exception {
			File temporaryFolder = Files.newTemporaryFolder();
			try {
				// @formatter:off
				TestDiscoveryRequest discoveryRequest = request()
						.selectors(
								selectClasspathRoots(singleton(temporaryFolder))
						).build();
				// @formatter:on

				List<String> folders = discoveryRequest.getSelectorsByType(ClasspathSelector.class).stream().map(
					ClasspathSelector::getClasspathRoot).map(File::getAbsolutePath).collect(toList());

				assertThat(folders).contains(temporaryFolder.getAbsolutePath());
			}
			finally {
				temporaryFolder.delete();
			}
		}

		@Test
		public void uniqueIdsAreStoredInDiscoveryRequest() throws Exception {
			UniqueId id1 = UniqueId.forEngine("engine").append("foo", "id1");
			UniqueId id2 = UniqueId.forEngine("engine").append("foo", "id2");

			// @formatter:off
			TestDiscoveryRequest discoveryRequest = request()
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
			TestDiscoveryRequest discoveryRequest = request()
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
			TestDiscoveryRequest discoveryRequest = request()
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
			TestDiscoveryRequest discoveryRequest = request()
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
			Exception exception = expectThrows(PreconditionViolationException.class,
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
			TestDiscoveryRequest discoveryRequest = request().build();

			ConfigurationParameters configurationParameters = discoveryRequest.getConfigurationParameters();
			assertThat(configurationParameters.get("key").isPresent()).isFalse();
		}

		@Test
		void configurationParameterAddedDirectly_isStoredInDiscoveryRequest() throws Exception {
			// @formatter:off
			TestDiscoveryRequest discoveryRequest = request()
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
			TestDiscoveryRequest discoveryRequest = request()
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
			TestDiscoveryRequest discoveryRequest = request()
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
			TestDiscoveryRequest discoveryRequest = request()
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
			TestDiscoveryRequest discoveryRequest = request()
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

}
