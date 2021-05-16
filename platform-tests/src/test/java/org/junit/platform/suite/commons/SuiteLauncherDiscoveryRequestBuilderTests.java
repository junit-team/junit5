/*
 * Copyright 2015-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.suite.commons;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.engine.JupiterTestEngine;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestTag;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.ClassNameFilter;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.discovery.ClasspathResourceSelector;
import org.junit.platform.engine.discovery.DirectorySelector;
import org.junit.platform.engine.discovery.FilePosition;
import org.junit.platform.engine.discovery.FileSelector;
import org.junit.platform.engine.discovery.ModuleSelector;
import org.junit.platform.engine.discovery.PackageNameFilter;
import org.junit.platform.engine.discovery.PackageSelector;
import org.junit.platform.engine.discovery.UriSelector;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.launcher.EngineFilter;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.PostDiscoveryFilter;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.DisableParentConfigurationParameters;
import org.junit.platform.suite.api.ExcludeClassNamePatterns;
import org.junit.platform.suite.api.ExcludeEngines;
import org.junit.platform.suite.api.ExcludePackages;
import org.junit.platform.suite.api.ExcludeTags;
import org.junit.platform.suite.api.IncludeClassNamePatterns;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.IncludePackages;
import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.SelectDirectories;
import org.junit.platform.suite.api.SelectFile;
import org.junit.platform.suite.api.SelectModules;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.SelectUris;

class SuiteLauncherDiscoveryRequestBuilderTests {

	SuiteLauncherDiscoveryRequestBuilder builder = SuiteLauncherDiscoveryRequestBuilder.request();

	@Test
	void configurationParameter() {
		@ConfigurationParameter(key = "com.example", value = "*")
		class Suite {
		}

		LauncherDiscoveryRequest request = builder.suite(Suite.class).build();
		ConfigurationParameters configuration = request.getConfigurationParameters();
		Optional<String> parameter = configuration.get("com.example");
		assertEquals(Optional.of("*"), parameter);
	}

	@Test
	void excludeClassNamePatterns() {
		class TestCase {
		}
		@ExcludeClassNamePatterns("^.*TestCase$")
		class Suite {
		}

		LauncherDiscoveryRequest request = builder.suite(Suite.class).build();
		List<ClassNameFilter> filters = request.getFiltersByType(ClassNameFilter.class);
		assertTrue(exactlyOne(filters).apply(TestCase.class.getName()).excluded());
	}

	@Test
	void excludeEngines() {
		@ExcludeEngines("junit-jupiter")
		class Suite {
		}

		LauncherDiscoveryRequest request = builder.suite(Suite.class).build();
		List<EngineFilter> filters = request.getEngineFilters();
		assertTrue(exactlyOne(filters).apply(new JupiterTestEngine()).excluded());
	}

	@Test
	void excludePackages() {
		@ExcludePackages("com.example.testcases")
		class Suite {
		}

		LauncherDiscoveryRequest request = builder.suite(Suite.class).build();
		List<PackageNameFilter> filters = request.getFiltersByType(PackageNameFilter.class);
		assertTrue(exactlyOne(filters).apply("com.example.testcases").excluded());
	}

	@Test
	void excludeTags() {
		@ExcludeTags("test-tag")
		class Suite {
		}

		LauncherDiscoveryRequest request = builder.suite(Suite.class).build();
		List<PostDiscoveryFilter> filters = request.getPostDiscoveryFilters();
		TestDescriptor testDescriptor = new StubAbstractTestDescriptor();
		assertTrue(exactlyOne(filters).apply(testDescriptor).excluded());
	}

	@Test
	void includeClassNamePatterns() {
		class TestCase {
		}
		@IncludeClassNamePatterns("^.*TestCase$")
		class Suite {
		}

		LauncherDiscoveryRequest request = builder.suite(Suite.class).build();
		List<ClassNameFilter> filters = request.getFiltersByType(ClassNameFilter.class);
		assertTrue(exactlyOne(filters).apply(TestCase.class.getName()).included());
		assertTrue(exactlyOne(filters).apply(Suite.class.getName()).excluded());
	}

	@Test
	void filtersOnStandardClassNamePatternsWhenIncludeClassNamePatternsIsOmitted() {
		class Suite {
		}

		LauncherDiscoveryRequest request = builder.suite(Suite.class).build();
		assertTrue(request.getFiltersByType(ClassNameFilter.class).isEmpty());
	}

	@Test
	void filtersOnStandardClassNamePatternsWhenIncludeClassNamePatternsIsOmittedUnlessDisabled() {
		class ExampleTest {
		}
		class Suite {
		}

		// @formatter:off
		LauncherDiscoveryRequest request = builder
				.filterStandardClassNamePatterns(true)
				.suite(Suite.class)
				.build();
		// @formatter:on
		List<ClassNameFilter> filters = request.getFiltersByType(ClassNameFilter.class);
		assertTrue(exactlyOne(filters).apply(ExampleTest.class.getName()).included());
		assertTrue(exactlyOne(filters).apply(Suite.class.getName()).excluded());
	}

	@Test
	void includeEngines() {
		@IncludeEngines("junit-jupiter")
		class Suite {
		}

		LauncherDiscoveryRequest request = builder.suite(Suite.class).build();
		List<EngineFilter> filters = request.getEngineFilters();
		assertTrue(exactlyOne(filters).apply(new JupiterTestEngine()).included());
	}

	@Test
	void includePackages() {
		@IncludePackages("com.example.testcases")
		class Suite {
		}

		LauncherDiscoveryRequest request = builder.suite(Suite.class).build();
		List<PackageNameFilter> filters = request.getFiltersByType(PackageNameFilter.class);
		assertTrue(exactlyOne(filters).apply("com.example.testcases").included());
	}

	@Test
	void includeTags() {
		@IncludeTags("test-tag")
		class Suite {
		}

		LauncherDiscoveryRequest request = builder.suite(Suite.class).build();
		List<PostDiscoveryFilter> filters = request.getPostDiscoveryFilters();
		TestDescriptor testDescriptor = new StubAbstractTestDescriptor();
		assertTrue(exactlyOne(filters).apply(testDescriptor).included());
	}

	@Test
	void selectClasses() {
		class TestCase {
		}
		@SelectClasses(TestCase.class)
		class Suite {
		}

		LauncherDiscoveryRequest request = builder.suite(Suite.class).build();
		List<ClassSelector> selectors = request.getSelectorsByType(ClassSelector.class);
		assertFalse(selectors.isEmpty());
		assertEquals(TestCase.class, exactlyOne(selectors).getJavaClass());
	}

	@Test
	void selectClasspathResource() {
		@SelectClasspathResource("com.example.testcases")
		class Suite {
		}

		LauncherDiscoveryRequest request = builder.suite(Suite.class).build();
		List<ClasspathResourceSelector> selectors = request.getSelectorsByType(ClasspathResourceSelector.class);
		assertEquals("com.example.testcases", exactlyOne(selectors).getClasspathResourceName());
	}

	@Test
	void selectClasspathResourcePosition() {
		@SelectClasspathResource(value = "com.example.testcases", line = 42)
		@SelectClasspathResource(value = "com.example.testcases", line = 14, column = 15)
		class Suite {
		}

		LauncherDiscoveryRequest request = builder.suite(Suite.class).build();
		List<ClasspathResourceSelector> selectors = request.getSelectorsByType(ClasspathResourceSelector.class);
		assertEquals(Optional.of(FilePosition.from(42)), selectors.get(0).getPosition());
		assertEquals(Optional.of(FilePosition.from(14, 15)), selectors.get(1).getPosition());
	}

	@Test
	void ignoreClasspathResourcePosition() {
		@SelectClasspathResource(value = "com.example.testcases", line = -1)
		@SelectClasspathResource(value = "com.example.testcases", column = 12)
		@SelectClasspathResource(value = "com.example.testcases", line = 42, column = -12)
		class Suite {
		}

		LauncherDiscoveryRequest request = builder.suite(Suite.class).build();
		List<ClasspathResourceSelector> selectors = request.getSelectorsByType(ClasspathResourceSelector.class);
		assertEquals(Optional.empty(), selectors.get(0).getPosition());
		assertEquals(Optional.empty(), selectors.get(1).getPosition());
		assertEquals(Optional.of(FilePosition.from(42)), selectors.get(2).getPosition());
	}

	@Test
	void selectDirectories() {
		@SelectDirectories("path/to/root")
		class Suite {
		}

		LauncherDiscoveryRequest request = builder.suite(Suite.class).build();
		List<DirectorySelector> selectors = request.getSelectorsByType(DirectorySelector.class);
		assertEquals(Paths.get("path/to/root"), exactlyOne(selectors).getPath());
	}

	@Test
	void selectDirectoriesFiltersEmptyPaths() {
		@SelectDirectories("")
		class Suite {
		}

		LauncherDiscoveryRequest request = builder.suite(Suite.class).build();
		assertTrue(request.getSelectorsByType(DirectorySelector.class).isEmpty());
	}

	@Test
	void selectFile() {
		@SelectFile("path/to/root")
		class Suite {
		}

		LauncherDiscoveryRequest request = builder.suite(Suite.class).build();
		List<FileSelector> selectors = request.getSelectorsByType(FileSelector.class);
		assertEquals(Paths.get("path/to/root"), exactlyOne(selectors).getPath());
	}

	@Test
	void selectFilePosition() {
		@SelectFile(value = "path/to/root", line = 42)
		@SelectFile(value = "path/to/root", line = 14, column = 15)
		class Suite {
		}

		LauncherDiscoveryRequest request = builder.suite(Suite.class).build();
		List<FileSelector> selectors = request.getSelectorsByType(FileSelector.class);
		assertEquals(Optional.of(FilePosition.from(42)), selectors.get(0).getPosition());
		assertEquals(Optional.of(FilePosition.from(14, 15)), selectors.get(1).getPosition());
	}

	@Test
	void ignoreInvalidFilePosition() {
		@SelectFile(value = "path/to/root", line = -1)
		@SelectFile(value = "path/to/root", column = 12)
		@SelectFile(value = "path/to/root", line = 42, column = -12)
		class Suite {
		}

		LauncherDiscoveryRequest request = builder.suite(Suite.class).build();
		List<FileSelector> selectors = request.getSelectorsByType(FileSelector.class);
		assertEquals(Optional.empty(), selectors.get(0).getPosition());
		assertEquals(Optional.empty(), selectors.get(1).getPosition());
		assertEquals(Optional.of(FilePosition.from(42)), selectors.get(2).getPosition());
	}

	@Test
	void selectModules() {
		@SelectModules("com.example.testcases")
		class Suite {
		}

		LauncherDiscoveryRequest request = builder.suite(Suite.class).build();
		List<ModuleSelector> selectors = request.getSelectorsByType(ModuleSelector.class);
		assertEquals("com.example.testcases", exactlyOne(selectors).getModuleName());
	}

	@Test
	void selectUris() {
		@SelectUris("path/to/root")
		class Suite {
		}

		LauncherDiscoveryRequest request = builder.suite(Suite.class).build();
		List<UriSelector> selectors = request.getSelectorsByType(UriSelector.class);
		assertEquals(URI.create("path/to/root"), exactlyOne(selectors).getUri());
	}

	@Test
	void selectUrisFiltersEmptyUris() {
		@SelectUris("")
		class Suite {
		}

		LauncherDiscoveryRequest request = builder.suite(Suite.class).build();
		assertTrue(request.getSelectorsByType(UriSelector.class).isEmpty());
	}

	@Test
	void selectPackages() {
		@SelectPackages("com.example.testcases")
		class Suite {
		}

		LauncherDiscoveryRequest request = builder.suite(Suite.class).build();
		List<PackageSelector> selectors = request.getSelectorsByType(PackageSelector.class);
		assertEquals("com.example.testcases", exactlyOne(selectors).getPackageName());
	}

	@SelectPackages("com.example.testcases")
	@Retention(RetentionPolicy.RUNTIME)
	@interface Meta {
	}

	@Test
	void metaAnnotations() {
		@Meta
		class Suite {
		}

		LauncherDiscoveryRequest request = builder.suite(Suite.class).build();
		List<PackageSelector> pSelectors = request.getSelectorsByType(PackageSelector.class);
		assertEquals("com.example.testcases", exactlyOne(pSelectors).getPackageName());
	}

	@Test
	void enableParentConfigurationParametersByDefault() {
		class Suite {

		}
		// @formatter:off
		var configuration = new ParentConfigurationParameters("parent", "parent parameters were used");
		var request = builder.suite(Suite.class)
				.parentConfigurationParameters(configuration)
				.build();
		// @formatter:on
		var configurationParameters = request.getConfigurationParameters();
		assertEquals(Optional.of("parent parameters were used"), configurationParameters.get("parent"));
	}

	@Test
	void disableParentConfigurationParameters() {
		@DisableParentConfigurationParameters
		class Suite {

		}
		// @formatter:off
		var configuration = new ParentConfigurationParameters("parent", "parent parameters were used");
		var request = builder.suite(Suite.class)
				.parentConfigurationParameters(configuration)
				.build();
		// @formatter:on
		var configurationParameters = request.getConfigurationParameters();
		assertEquals(Optional.empty(), configurationParameters.get("parent"));
	}

	private static <T> T exactlyOne(List<T> list) {
		assertEquals(1, list.size());
		return list.get(0);
	}

	private static class StubAbstractTestDescriptor extends AbstractTestDescriptor {
		public StubAbstractTestDescriptor() {
			super(UniqueId.forEngine("test"), "stub");
		}

		@Override
		public Type getType() {
			return Type.CONTAINER;
		}

		@Override
		public Set<TestTag> getTags() {
			return Collections.singleton(TestTag.create("test-tag"));
		}

	}

	private static class ParentConfigurationParameters implements ConfigurationParameters {
		private final Map<String, String> map;

		public ParentConfigurationParameters(String key, String value) {
			this.map = Map.of(key, value);
		}

		@Override
		public Optional<String> get(String key) {
			return Optional.ofNullable(map.get(key));
		}

		@Override
		public Optional<Boolean> getBoolean(String key) {
			return Optional.empty();
		}

		@Override
		public int size() {
			return map.size();
		}

	}

}
