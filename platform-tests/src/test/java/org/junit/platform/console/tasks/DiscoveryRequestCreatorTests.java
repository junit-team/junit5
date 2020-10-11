/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.console.tasks;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.platform.engine.discovery.ClassNameFilter.STANDARD_INCLUDE_PATTERN;

import java.io.File;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.console.options.CommandLineOptions;
import org.junit.platform.engine.Filter;
import org.junit.platform.engine.discovery.ClassNameFilter;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.discovery.ClasspathResourceSelector;
import org.junit.platform.engine.discovery.ClasspathRootSelector;
import org.junit.platform.engine.discovery.DirectorySelector;
import org.junit.platform.engine.discovery.FileSelector;
import org.junit.platform.engine.discovery.MethodSelector;
import org.junit.platform.engine.discovery.PackageNameFilter;
import org.junit.platform.engine.discovery.PackageSelector;
import org.junit.platform.engine.discovery.UriSelector;
import org.junit.platform.launcher.LauncherDiscoveryRequest;

/**
 * @since 1.0
 */
class DiscoveryRequestCreatorTests {

	private final CommandLineOptions options = new CommandLineOptions();

	@Test
	void convertsScanClasspathOptionWithoutExplicitRootDirectories() {
		options.setScanClasspath(true);

		var request = convert();

		var classpathRootSelectors = request.getSelectorsByType(ClasspathRootSelector.class);
		// @formatter:off
		assertThat(classpathRootSelectors).extracting(ClasspathRootSelector::getClasspathRoot)
				.hasAtLeastOneElementOfType(URI.class)
				.doesNotContainNull();
		// @formatter:on
	}

	@Test
	void convertsScanClasspathOptionWithExplicitRootDirectories() {
		options.setScanClasspath(true);
		options.setSelectedClasspathEntries(asList(Paths.get("."), Paths.get("..")));

		var request = convert();

		var classpathRootSelectors = request.getSelectorsByType(ClasspathRootSelector.class);
		// @formatter:off
		assertThat(classpathRootSelectors).extracting(ClasspathRootSelector::getClasspathRoot)
				.containsExactly(new File(".").toURI(), new File("..").toURI());
		// @formatter:on
	}

	@Test
	void convertsScanClasspathOptionWithAdditionalClasspathEntries() {
		options.setScanClasspath(true);
		options.setAdditionalClasspathEntries(asList(Paths.get("."), Paths.get("..")));

		var request = convert();

		var classpathRootSelectors = request.getSelectorsByType(ClasspathRootSelector.class);
		// @formatter:off
		assertThat(classpathRootSelectors).extracting(ClasspathRootSelector::getClasspathRoot)
			.contains(new File(".").toURI(), new File("..").toURI());
		// @formatter:on
	}

	@Test
	void doesNotSupportScanClasspathAndExplicitSelectors() {
		options.setScanClasspath(true);
		options.setSelectedClasses(singletonList("SomeTest"));

		Throwable cause = assertThrows(PreconditionViolationException.class, this::convert);

		assertThat(cause).hasMessageContaining("not supported");
	}

	@Test
	void convertsDefaultIncludeClassNamePatternOption() {
		options.setScanClasspath(true);

		var request = convert();

		var filters = request.getFiltersByType(ClassNameFilter.class);
		assertThat(filters).hasSize(1);
		assertExcludes(filters.get(0), STANDARD_INCLUDE_PATTERN);
	}

	@Test
	void convertsExplicitIncludeClassNamePatternOption() {
		options.setScanClasspath(true);
		options.setIncludedClassNamePatterns(asList("Foo.*Bar", "Bar.*Foo"));

		var request = convert();

		var filters = request.getFiltersByType(ClassNameFilter.class);
		assertThat(filters).hasSize(1);
		assertIncludes(filters.get(0), "Foo.*Bar");
		assertIncludes(filters.get(0), "Bar.*Foo");
	}

	@Test
	void includeSelectedClassesAndMethodsRegardlessOfClassNamePatterns() {
		options.setSelectedClasses(singletonList("SomeTest"));
		options.setSelectedMethods(asList("com.acme.Foo#m()"));
		options.setIncludedClassNamePatterns(asList("Foo.*Bar"));

		var request = convert();

		var filters = request.getFiltersByType(ClassNameFilter.class);
		assertThat(filters).hasSize(1);
		assertIncludes(filters.get(0), "SomeTest");
		assertIncludes(filters.get(0), "com.acme.Foo");
		assertIncludes(filters.get(0), "Foo.*Bar");
	}

	@Test
	void convertsExcludeClassNamePatternOption() {
		options.setScanClasspath(true);
		options.setExcludedClassNamePatterns(asList("Foo.*Bar", "Bar.*Foo"));

		var request = convert();

		var filters = request.getFiltersByType(ClassNameFilter.class);
		assertThat(filters).hasSize(2);
		assertExcludes(filters.get(1), "Foo.*Bar");
		assertExcludes(filters.get(1), "Bar.*Foo");
	}

	@Test
	void convertsPackageOptions() {
		options.setScanClasspath(true);
		options.setIncludedPackages(asList("org.junit.included1", "org.junit.included2", "org.junit.included3"));
		options.setExcludedPackages(asList("org.junit.excluded1"));

		var request = convert();
		var packageNameFilters = request.getFiltersByType(PackageNameFilter.class);

		assertThat(packageNameFilters).hasSize(2);
		assertIncludes(packageNameFilters.get(0), "org.junit.included1");
		assertIncludes(packageNameFilters.get(0), "org.junit.included2");
		assertIncludes(packageNameFilters.get(0), "org.junit.included3");
		assertExcludes(packageNameFilters.get(1), "org.junit.excluded1");
	}

	@Test
	void convertsTagOptions() {
		options.setScanClasspath(true);
		options.setIncludedTagExpressions(asList("fast", "medium", "slow"));
		options.setExcludedTagExpressions(asList("slow"));

		var request = convert();
		var postDiscoveryFilters = request.getPostDiscoveryFilters();

		assertThat(postDiscoveryFilters).hasSize(2);
		assertThat(postDiscoveryFilters.get(0).toString()).contains("TagFilter");
		assertThat(postDiscoveryFilters.get(1).toString()).contains("TagFilter");
	}

	@Test
	void convertsEngineOptions() {
		options.setScanClasspath(true);
		options.setIncludedEngines(asList("engine1", "engine2", "engine3"));
		options.setExcludedEngines(singletonList("engine2"));

		var request = convert();
		var engineFilters = request.getEngineFilters();

		assertThat(engineFilters).hasSize(2);
		assertThat(engineFilters.get(0).toString()).contains("includes", "[engine1, engine2, engine3]");
		assertThat(engineFilters.get(1).toString()).contains("excludes", "[engine2]");
	}

	@Test
	void convertsUriSelectors() {
		options.setSelectedUris(asList(URI.create("a"), URI.create("b")));

		var request = convert();
		var uriSelectors = request.getSelectorsByType(UriSelector.class);

		assertThat(uriSelectors).extracting(UriSelector::getUri).containsExactly(URI.create("a"), URI.create("b"));
	}

	@Test
	void convertsFileSelectors() {
		options.setSelectedFiles(asList("foo.txt", "bar.csv"));

		var request = convert();
		var fileSelectors = request.getSelectorsByType(FileSelector.class);

		assertThat(fileSelectors).extracting(FileSelector::getRawPath).containsExactly("foo.txt", "bar.csv");
	}

	@Test
	void convertsDirectorySelectors() {
		options.setSelectedDirectories(asList("foo/bar", "bar/qux"));

		var request = convert();
		var directorySelectors = request.getSelectorsByType(DirectorySelector.class);

		assertThat(directorySelectors).extracting(DirectorySelector::getRawPath).containsExactly("foo/bar", "bar/qux");
	}

	@Test
	void convertsPackageSelectors() {
		options.setSelectedPackages(asList("com.acme.foo", "com.example.bar"));

		var request = convert();
		var packageSelectors = request.getSelectorsByType(PackageSelector.class);

		assertThat(packageSelectors).extracting(PackageSelector::getPackageName).containsExactly("com.acme.foo",
			"com.example.bar");
	}

	@Test
	void convertsClassSelectors() {
		options.setSelectedClasses(asList("com.acme.Foo", "com.example.Bar"));

		var request = convert();
		var classSelectors = request.getSelectorsByType(ClassSelector.class);

		assertThat(classSelectors).extracting(ClassSelector::getClassName).containsExactly("com.acme.Foo",
			"com.example.Bar");
	}

	@Test
	void convertsMethodSelectors() {
		options.setSelectedMethods(asList("com.acme.Foo#m()", "com.example.Bar#method(java.lang.Object)"));

		var request = convert();
		var methodSelectors = request.getSelectorsByType(MethodSelector.class);

		assertThat(methodSelectors).hasSize(2);
		assertThat(methodSelectors.get(0).getClassName()).isEqualTo("com.acme.Foo");
		assertThat(methodSelectors.get(0).getMethodName()).isEqualTo("m");
		assertThat(methodSelectors.get(0).getMethodParameterTypes()).isEqualTo("");
		assertThat(methodSelectors.get(1).getClassName()).isEqualTo("com.example.Bar");
		assertThat(methodSelectors.get(1).getMethodName()).isEqualTo("method");
		assertThat(methodSelectors.get(1).getMethodParameterTypes()).isEqualTo("java.lang.Object");
	}

	@Test
	void convertsClasspathResourceSelectors() {
		options.setSelectedClasspathResources(asList("foo.csv", "com/example/bar.json"));

		var request = convert();
		var classpathResourceSelectors = request.getSelectorsByType(ClasspathResourceSelector.class);

		assertThat(classpathResourceSelectors).extracting(
			ClasspathResourceSelector::getClasspathResourceName).containsExactly("foo.csv", "com/example/bar.json");
	}

	@Test
	void convertsConfigurationParameters() {
		options.setScanClasspath(true);
		options.setConfigurationParameters(mapOf(entry("foo", "bar"), entry("baz", "true")));

		var request = convert();
		var configurationParameters = request.getConfigurationParameters();

		assertThat(configurationParameters.size()).isEqualTo(2);
		assertThat(configurationParameters.get("foo")).contains("bar");
		assertThat(configurationParameters.getBoolean("baz")).contains(true);
	}

	private LauncherDiscoveryRequest convert() {
		var creator = new DiscoveryRequestCreator();
		return creator.toDiscoveryRequest(options);
	}

	private void assertIncludes(Filter<String> filter, String included) {
		assertThat(filter.apply(included).included()).isTrue();
	}

	private void assertExcludes(Filter<String> filter, String excluded) {
		assertThat(filter.apply(excluded).excluded()).isTrue();
	}

	@SafeVarargs
	@SuppressWarnings("varargs")
	private static <K, V> Map<K, V> mapOf(Entry<K, V>... entries) {
		return Stream.of(entries).collect(toMap(Entry::getKey, Entry::getValue));
	}

}
