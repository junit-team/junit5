/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.console.tasks;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.platform.engine.discovery.ClassNameFilter.STANDARD_INCLUDE_PATTERN;

import java.io.File;
import java.net.URI;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.PreconditionViolationException;
import org.junit.platform.console.options.CommandLineOptions;
import org.junit.platform.engine.discovery.ClassNameFilter;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.discovery.ClasspathResourceSelector;
import org.junit.platform.engine.discovery.ClasspathRootSelector;
import org.junit.platform.engine.discovery.DirectorySelector;
import org.junit.platform.engine.discovery.FileSelector;
import org.junit.platform.engine.discovery.MethodSelector;
import org.junit.platform.engine.discovery.PackageSelector;
import org.junit.platform.engine.discovery.UriSelector;
import org.junit.platform.launcher.EngineFilter;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.PostDiscoveryFilter;

/**
 * @since 1.0
 */
public class DiscoveryRequestCreatorTests {

	private final CommandLineOptions options = new CommandLineOptions();

	@Test
	public void convertsScanClasspathOptionWithoutExplicitRootDirectories() {
		options.setScanClasspath(true);

		LauncherDiscoveryRequest request = convert();

		List<ClasspathRootSelector> classpathRootSelectors = request.getSelectorsByType(ClasspathRootSelector.class);
		// @formatter:off
		assertThat(classpathRootSelectors).extracting(ClasspathRootSelector::getClasspathRoot)
				.hasAtLeastOneElementOfType(URI.class)
				.doesNotContainNull();
		// @formatter:on
	}

	@Test
	public void convertsScanClasspathOptionWithExplicitRootDirectories() {
		options.setScanClasspath(true);
		options.setSelectedClasspathEntries(asList(Paths.get("."), Paths.get("..")));

		LauncherDiscoveryRequest request = convert();

		List<ClasspathRootSelector> classpathRootSelectors = request.getSelectorsByType(ClasspathRootSelector.class);
		// @formatter:off
		assertThat(classpathRootSelectors).extracting(ClasspathRootSelector::getClasspathRoot)
				.containsExactly(new File(".").toURI(), new File("..").toURI());
		// @formatter:on
	}

	@Test
	public void convertsScanClasspathOptionWithAdditionalClasspathEntries() {
		options.setScanClasspath(true);
		options.setAdditionalClasspathEntries(asList(Paths.get("."), Paths.get("..")));

		LauncherDiscoveryRequest request = convert();

		List<ClasspathRootSelector> classpathRootSelectors = request.getSelectorsByType(ClasspathRootSelector.class);
		// @formatter:off
		assertThat(classpathRootSelectors).extracting(ClasspathRootSelector::getClasspathRoot)
			.contains(new File(".").toURI(), new File("..").toURI());
		// @formatter:on
	}

	@Test
	public void doesNotSupportScanClasspathAndExplicitSelectors() {
		options.setScanClasspath(true);
		options.setSelectedClasses(singletonList("SomeTest"));

		Throwable cause = assertThrows(PreconditionViolationException.class, this::convert);

		assertThat(cause).hasMessageContaining("not supported");
	}

	@Test
	public void convertsDefaultIncludeClassNamePatternOption() {
		options.setScanClasspath(true);

		LauncherDiscoveryRequest request = convert();

		List<ClassNameFilter> filter = request.getDiscoveryFiltersByType(ClassNameFilter.class);
		assertThat(filter).hasSize(1);
		assertThat(filter.get(0).toString()).contains(STANDARD_INCLUDE_PATTERN);
	}

	@Test
	public void convertsExplicitIncludeClassNamePatternOption() {
		options.setScanClasspath(true);
		options.setIncludedClassNamePatterns(asList("Foo.*Bar", "Bar.*Foo"));

		LauncherDiscoveryRequest request = convert();

		List<ClassNameFilter> filter = request.getDiscoveryFiltersByType(ClassNameFilter.class);
		assertThat(filter).hasSize(1);
		assertThat(filter.get(0).toString()).contains("Foo.*Bar");
		assertThat(filter.get(0).toString()).contains("Bar.*Foo");
	}

	@Test
	public void convertsTagOptions() {
		options.setScanClasspath(true);
		options.setIncludedTags(asList("fast", "medium", "slow"));
		options.setExcludedTags(asList("slow"));

		LauncherDiscoveryRequest request = convert();
		List<PostDiscoveryFilter> postDiscoveryFilters = request.getPostDiscoveryFilters();

		assertThat(postDiscoveryFilters).hasSize(2);
		assertThat(postDiscoveryFilters.get(0).toString()).contains("TagFilter");
		assertThat(postDiscoveryFilters.get(1).toString()).contains("TagFilter");
	}

	@Test
	public void convertsEngineOptions() {
		options.setScanClasspath(true);
		options.setIncludedEngines(asList("engine1", "engine2", "engine3"));
		options.setExcludedEngines(singletonList("engine2"));

		LauncherDiscoveryRequest request = convert();
		List<EngineFilter> engineFilters = request.getEngineFilters();

		assertThat(engineFilters).hasSize(2);
		assertThat(engineFilters.get(0).toString()).contains("includes", "[engine1, engine2, engine3]");
		assertThat(engineFilters.get(1).toString()).contains("excludes", "[engine2]");
	}

	@Test
	public void convertsUriSelectors() {
		options.setSelectedUris(asList(URI.create("a"), URI.create("b")));

		LauncherDiscoveryRequest request = convert();
		List<UriSelector> uriSelectors = request.getSelectorsByType(UriSelector.class);

		assertThat(uriSelectors).extracting(UriSelector::getUri).containsExactly(URI.create("a"), URI.create("b"));
	}

	@Test
	public void convertsFileSelectors() {
		options.setSelectedFiles(asList("foo.txt", "bar.csv"));

		LauncherDiscoveryRequest request = convert();
		List<FileSelector> fileSelectors = request.getSelectorsByType(FileSelector.class);

		assertThat(fileSelectors).extracting(FileSelector::getRawPath).containsExactly("foo.txt", "bar.csv");
	}

	@Test
	public void convertsDirectorySelectors() {
		options.setSelectedDirectories(asList("foo/bar", "bar/qux"));

		LauncherDiscoveryRequest request = convert();
		List<DirectorySelector> directorySelectors = request.getSelectorsByType(DirectorySelector.class);

		assertThat(directorySelectors).extracting(DirectorySelector::getRawPath).containsExactly("foo/bar", "bar/qux");
	}

	@Test
	public void convertsPackageSelectors() {
		options.setSelectedPackages(asList("com.acme.foo", "com.example.bar"));

		LauncherDiscoveryRequest request = convert();
		List<PackageSelector> packageSelectors = request.getSelectorsByType(PackageSelector.class);

		assertThat(packageSelectors).extracting(PackageSelector::getPackageName).containsExactly("com.acme.foo",
			"com.example.bar");
	}

	@Test
	public void convertsClassSelectors() {
		options.setSelectedClasses(asList("com.acme.Foo", "com.example.Bar"));

		LauncherDiscoveryRequest request = convert();
		List<ClassSelector> classSelectors = request.getSelectorsByType(ClassSelector.class);

		assertThat(classSelectors).extracting(ClassSelector::getClassName).containsExactly("com.acme.Foo",
			"com.example.Bar");
	}

	@Test
	public void convertsMethodSelectors() {
		options.setSelectedMethods(asList("com.acme.Foo#m()", "com.example.Bar#method(java.lang.Object)"));

		LauncherDiscoveryRequest request = convert();
		List<MethodSelector> methodSelectors = request.getSelectorsByType(MethodSelector.class);

		assertThat(methodSelectors).hasSize(2);
		assertThat(methodSelectors.get(0).getClassName()).isEqualTo("com.acme.Foo");
		assertThat(methodSelectors.get(0).getMethodName()).isEqualTo("m");
		assertThat(methodSelectors.get(0).getMethodParameterTypes()).isNull();
		assertThat(methodSelectors.get(1).getClassName()).isEqualTo("com.example.Bar");
		assertThat(methodSelectors.get(1).getMethodName()).isEqualTo("method");
		assertThat(methodSelectors.get(1).getMethodParameterTypes()).isEqualTo("java.lang.Object");
	}

	@Test
	public void convertsClasspathResourceSelectors() {
		options.setSelectedClasspathResources(asList("foo.csv", "com/example/bar.json"));

		LauncherDiscoveryRequest request = convert();
		List<ClasspathResourceSelector> classpathResourceSelectors = request.getSelectorsByType(
			ClasspathResourceSelector.class);

		assertThat(classpathResourceSelectors).extracting(
			ClasspathResourceSelector::getClasspathResourceName).containsExactly("foo.csv", "com/example/bar.json");
	}

	private LauncherDiscoveryRequest convert() {
		DiscoveryRequestCreator creator = new DiscoveryRequestCreator();
		return creator.toDiscoveryRequest(options);
	}

}
