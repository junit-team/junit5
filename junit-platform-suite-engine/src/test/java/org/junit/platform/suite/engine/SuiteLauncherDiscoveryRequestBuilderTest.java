/*
 * Copyright 2015-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.suite.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.engine.Constants.DEACTIVATE_ALL_CONDITIONS_PATTERN;
import static org.junit.jupiter.engine.Constants.DEACTIVATE_CONDITIONS_PATTERN_PROPERTY_NAME;

import java.net.URI;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
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
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.suite.engine.testcases.SimpleTest;
import org.junit.platform.suite.engine.testsuites.ConfigurationSuite;
import org.junit.platform.suite.engine.testsuites.ExcludeClassNamePatternsSuite;
import org.junit.platform.suite.engine.testsuites.ExcludeEnginesSuite;
import org.junit.platform.suite.engine.testsuites.ExcludePackagesSuite;
import org.junit.platform.suite.engine.testsuites.ExcludeTagsSuite;
import org.junit.platform.suite.engine.testsuites.IncludeClassNamePatternsSuite;
import org.junit.platform.suite.engine.testsuites.IncludeJupiterEnginesSuite;
import org.junit.platform.suite.engine.testsuites.IncludePackagesSuite;
import org.junit.platform.suite.engine.testsuites.IncludeTagsSuite;
import org.junit.platform.suite.engine.testsuites.SelectClassesSuite;
import org.junit.platform.suite.engine.testsuites.SelectClasspathResourcePositionSuite;
import org.junit.platform.suite.engine.testsuites.SelectClasspathResourceSuite;
import org.junit.platform.suite.engine.testsuites.SelectDirectoriesSuite;
import org.junit.platform.suite.engine.testsuites.SelectFilePositionSuite;
import org.junit.platform.suite.engine.testsuites.SelectFileSuite;
import org.junit.platform.suite.engine.testsuites.SelectInvalidClasspathResourcePositionSuite;
import org.junit.platform.suite.engine.testsuites.SelectInvalidFilePositionSuite;
import org.junit.platform.suite.engine.testsuites.SelectModuleSuite;
import org.junit.platform.suite.engine.testsuites.SelectPackageSuite;
import org.junit.platform.suite.engine.testsuites.SelectUriSuite;

class SuiteLauncherDiscoveryRequestBuilderTest {

	LauncherDiscoveryRequestBuilder launcherRequest = LauncherDiscoveryRequestBuilder.request();
	SuiteLauncherDiscoveryRequestBuilder builder = SuiteLauncherDiscoveryRequestBuilder.request(launcherRequest);

	@Test
	void configuration() {
		LauncherDiscoveryRequest request = builder.suite(ConfigurationSuite.class).build();
		ConfigurationParameters configuration = request.getConfigurationParameters();
		Optional<String> parameter = configuration.get(DEACTIVATE_CONDITIONS_PATTERN_PROPERTY_NAME);
		assertEquals(Optional.of(DEACTIVATE_ALL_CONDITIONS_PATTERN), parameter);
	}

	@Test
	void excludeClassNamePatterns() {
		LauncherDiscoveryRequest request = builder.suite(ExcludeClassNamePatternsSuite.class).build();
		List<ClassNameFilter> filters = request.getFiltersByType(ClassNameFilter.class);
		assertTrue(filters.stream().anyMatch(filter -> filter.apply(SimpleTest.class.getName()).excluded()));
	}

	@Test
	void excludeEngines() {
		LauncherDiscoveryRequest request = builder.suite(ExcludeEnginesSuite.class).build();
		List<EngineFilter> filters = request.getEngineFilters();
		filters.forEach(filter -> assertTrue(filter.apply(new JupiterTestEngine()).excluded()));
	}

	@Test
	void excludePackages() {
		LauncherDiscoveryRequest request = builder.suite(ExcludePackagesSuite.class).build();
		List<PackageNameFilter> filters = request.getFiltersByType(PackageNameFilter.class);
		filters.forEach(filter -> assertTrue(filter.apply("org.junit.platform.suite.engine.testcases").excluded()));
	}

	@Test
	void excludeTags() {
		LauncherDiscoveryRequest request = builder.suite(ExcludeTagsSuite.class).build();
		List<PostDiscoveryFilter> filters = request.getPostDiscoveryFilters();
		TestDescriptor testDescriptor = new StubAbstractTestDescriptor();
		filters.forEach(filter -> assertTrue(filter.apply(testDescriptor).excluded()));
	}

	@Test
	void includeClassNamePatterns() {
		LauncherDiscoveryRequest request = builder.suite(IncludeClassNamePatternsSuite.class).build();
		List<ClassNameFilter> filters = request.getFiltersByType(ClassNameFilter.class);
		filters.forEach(filter -> assertTrue(filter.apply(SimpleTest.class.getName()).included()));
	}

	@Test
	void includesStandardIncludePatternsWhenIncludeClassNamePatternsIsOmitted() {
		LauncherDiscoveryRequest request = builder.suite(SelectClassesSuite.class).build();
		List<ClassNameFilter> filters = request.getFiltersByType(ClassNameFilter.class);
		filters.forEach(filter -> assertTrue(filter.apply(SimpleTest.class.getName()).included()));
		filters.forEach(filter -> assertTrue(filter.apply(SelectClassesSuite.class.getName()).excluded()));
	}

	@Test
	void includeEngines() {
		LauncherDiscoveryRequest request = builder.suite(IncludeJupiterEnginesSuite.class).build();
		List<EngineFilter> filters = request.getEngineFilters();
		filters.forEach(filter -> assertTrue(filter.apply(new JupiterTestEngine()).included()));
	}

	@Test
	void includePackages() {
		LauncherDiscoveryRequest request = builder.suite(IncludePackagesSuite.class).build();
		List<PackageNameFilter> filters = request.getFiltersByType(PackageNameFilter.class);
		filters.forEach(filter -> assertTrue(filter.apply("org.junit.platform.suite.engine.testcases").included()));
	}

	@Test
	void includeTags() {
		LauncherDiscoveryRequest request = builder.suite(IncludeTagsSuite.class).build();
		List<PostDiscoveryFilter> filters = request.getPostDiscoveryFilters();
		TestDescriptor testDescriptor = new StubAbstractTestDescriptor();
		filters.forEach(filter -> assertTrue(filter.apply(testDescriptor).included()));
	}

	@Test
	void selectClasses() {
		LauncherDiscoveryRequest request = builder.suite(SelectClassesSuite.class).build();
		List<ClassSelector> selectors = request.getSelectorsByType(ClassSelector.class);
		selectors.forEach(selector -> assertEquals(SimpleTest.class, selector.getJavaClass()));
	}

	@Test
	void selectClasspathResource() {
		LauncherDiscoveryRequest request = builder.suite(SelectClasspathResourceSuite.class).build();
		List<ClasspathResourceSelector> selectors = request.getSelectorsByType(ClasspathResourceSelector.class);
		selectors.forEach(
			selector -> assertEquals("org.junit.platform.suite.engine.testcases", selector.getClasspathResourceName()));
	}

	@Test
	void selectClasspathResourcePosition() {
		LauncherDiscoveryRequest request = builder.suite(SelectClasspathResourcePositionSuite.class).build();
		List<ClasspathResourceSelector> selectors = request.getSelectorsByType(ClasspathResourceSelector.class);
		assertEquals(Optional.of(FilePosition.from(42)), selectors.get(0).getPosition());
		assertEquals(Optional.of(FilePosition.from(14, 15)), selectors.get(1).getPosition());
	}

	@Test
	void ignoreClasspathResourcePosition() {
		LauncherDiscoveryRequest request = builder.suite(SelectInvalidClasspathResourcePositionSuite.class).build();
		List<ClasspathResourceSelector> selectors = request.getSelectorsByType(ClasspathResourceSelector.class);
		assertEquals(Optional.empty(), selectors.get(0).getPosition());
		assertEquals(Optional.empty(), selectors.get(1).getPosition());
		assertEquals(Optional.of(FilePosition.from(42)), selectors.get(2).getPosition());
	}

	@Test
	void selectDirectories() {
		LauncherDiscoveryRequest request = builder.suite(SelectDirectoriesSuite.class).build();
		List<DirectorySelector> selectors = request.getSelectorsByType(DirectorySelector.class);
		selectors.forEach(selector -> assertEquals(Paths.get("path/to/root"), selector.getPath()));
	}

	@Test
	void selectFile() {
		LauncherDiscoveryRequest request = builder.suite(SelectFileSuite.class).build();
		List<FileSelector> selectors = request.getSelectorsByType(FileSelector.class);
		selectors.forEach(selector -> assertEquals(Paths.get("path/to/root"), selector.getPath()));
	}

	@Test
	void selectFilePosition() {
		LauncherDiscoveryRequest request = builder.suite(SelectFilePositionSuite.class).build();
		List<FileSelector> selectors = request.getSelectorsByType(FileSelector.class);
		assertEquals(Optional.of(FilePosition.from(42)), selectors.get(0).getPosition());
		assertEquals(Optional.of(FilePosition.from(14, 15)), selectors.get(1).getPosition());
	}

	@Test
	void ignoreInvalidFilePosition() {
		LauncherDiscoveryRequest request = builder.suite(SelectInvalidFilePositionSuite.class).build();
		List<FileSelector> selectors = request.getSelectorsByType(FileSelector.class);
		assertEquals(Optional.empty(), selectors.get(0).getPosition());
		assertEquals(Optional.empty(), selectors.get(1).getPosition());
		assertEquals(Optional.of(FilePosition.from(42)), selectors.get(2).getPosition());
	}

	@Test
	void selectModules() {
		LauncherDiscoveryRequest request = builder.suite(SelectModuleSuite.class).build();
		List<ModuleSelector> selectors = request.getSelectorsByType(ModuleSelector.class);
		selectors.forEach(
			selector -> assertEquals("org.junit.platform.suite.engine.testcases", selector.getModuleName()));
	}

	@Test
	void selectUris() {
		LauncherDiscoveryRequest request = builder.suite(SelectUriSuite.class).build();
		List<UriSelector> selectors = request.getSelectorsByType(UriSelector.class);
		selectors.forEach(selector -> assertEquals(URI.create("path/to/root"), selector.getUri()));
	}

	@Test
	void selectPackages() {
		LauncherDiscoveryRequest request = builder.suite(SelectPackageSuite.class).build();
		List<PackageSelector> selectors = request.getSelectorsByType(PackageSelector.class);
		selectors.forEach(
			selector -> assertEquals("org.junit.platform.suite.engine.testcases", selector.getPackageName()));
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

}
