/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.suite.engine;

import static java.util.stream.Collectors.toList;
import static org.junit.platform.commons.support.AnnotationSupport.findAnnotation;
import static org.junit.platform.commons.support.AnnotationSupport.findRepeatableAnnotations;
import static org.junit.platform.engine.discovery.ClassNameFilter.excludeClassNamePatterns;
import static org.junit.platform.engine.discovery.ClassNameFilter.includeClassNamePatterns;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClasspathResource;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectDirectory;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectFile;
import static org.junit.platform.engine.discovery.PackageNameFilter.excludePackageNames;
import static org.junit.platform.engine.discovery.PackageNameFilter.includePackageNames;
import static org.junit.platform.launcher.EngineFilter.excludeEngines;
import static org.junit.platform.launcher.EngineFilter.includeEngines;
import static org.junit.platform.launcher.TagFilter.excludeTags;
import static org.junit.platform.launcher.TagFilter.includeTags;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.discovery.ClasspathResourceSelector;
import org.junit.platform.engine.discovery.ClasspathRootSelector;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.engine.discovery.FilePosition;
import org.junit.platform.engine.discovery.FileSelector;
import org.junit.platform.engine.discovery.ModuleSelector;
import org.junit.platform.engine.discovery.PackageSelector;
import org.junit.platform.engine.discovery.UriSelector;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.suite.api.Configuration;
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
import org.junit.platform.suite.api.SelectClasspathRoots;
import org.junit.platform.suite.api.SelectDirectories;
import org.junit.platform.suite.api.SelectFile;
import org.junit.platform.suite.api.SelectModules;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.SelectUris;

final class SuiteLauncherDiscoveryRequestBuilder {

	private final LauncherDiscoveryRequestBuilder request = new LauncherDiscoveryRequestBuilder();

	SuiteLauncherDiscoveryRequestBuilder configureRequestingSuiteId(UniqueId uniqueId) {
		request.configurationParameter(SuiteConfiguration.PARENT_SUITE_ID, uniqueId.toString());
		return this;
	}

	SuiteLauncherDiscoveryRequestBuilder addRequestFrom(UniqueId uniqueId) {
		request.selectors(DiscoverySelectors.selectUniqueId(uniqueId));
		return this;
	}

	SuiteLauncherDiscoveryRequestBuilder addRequestFrom(Class<?> testClass) {
		// Annotations in alphabetical order
		findRepeatableAnnotations(testClass, Configuration.class).forEach(
			configuration -> request.configurationParameter(configuration.key(), configuration.value()));
		findAnnotation(testClass, ExcludeClassNamePatterns.class).map(ExcludeClassNamePatterns::value).ifPresent(
			patterns -> request.filters(excludeClassNamePatterns(patterns)));
		findAnnotation(testClass, ExcludeEngines.class).map(ExcludeEngines::value).ifPresent(
			engineIds -> request.filters(excludeEngines(engineIds)));
		findAnnotation(testClass, ExcludePackages.class).map(ExcludePackages::value).ifPresent(
			packageNames -> request.filters(excludePackageNames(packageNames)));
		findAnnotation(testClass, ExcludeTags.class).map(ExcludeTags::value).ifPresent(
			tagExpressions -> request.filters(excludeTags(tagExpressions)));
		findAnnotation(testClass, IncludeClassNamePatterns.class).map(IncludeClassNamePatterns::value).ifPresent(
			patterns -> request.filters(includeClassNamePatterns(patterns)));
		findAnnotation(testClass, IncludeEngines.class).map(IncludeEngines::value).ifPresent(
			engineIds -> request.filters(includeEngines(engineIds)));
		findAnnotation(testClass, IncludePackages.class).map(IncludePackages::value).ifPresent(
			packageNames -> request.filters(includePackageNames(packageNames)));
		findAnnotation(testClass, IncludeTags.class).map(IncludeTags::value).ifPresent(
			tagExpressions -> request.filters(includeTags(tagExpressions)));
		findAnnotation(testClass, SelectClasses.class).map(SelectClasses::value).ifPresent(
			classes -> request.selectors(selectClasses(classes)));
		findRepeatableAnnotations(testClass, SelectClasspathResource.class).forEach(
			resource -> request.selectors(selectClasspathResourceAndFilePosition(resource)));
		findAnnotation(testClass, SelectClasspathRoots.class).map(SelectClasspathRoots::value).ifPresent(
			classPathRoots -> request.selectors(selectClasspathRoots(classPathRoots)));
		findAnnotation(testClass, SelectDirectories.class).map(SelectDirectories::value).ifPresent(
			directory -> request.selectors(selectDirectory(directory)));
		findRepeatableAnnotations(testClass, SelectFile.class).forEach(
			file -> request.selectors(selectFileAndPosition(file)));
		findAnnotation(testClass, SelectModules.class).map(SelectModules::value).ifPresent(
			modules -> request.selectors(selectModules(modules)));
		findAnnotation(testClass, SelectUris.class).map(SelectUris::value).ifPresent(
			uris -> request.selectors(selectUris(uris)));
		findAnnotation(testClass, SelectPackages.class).map(SelectPackages::value).ifPresent(
			packages -> request.selectors(selectPackages(packages)));

		return this;
	}

	LauncherDiscoveryRequest build() {
		return request.build();
	}

	private ClasspathResourceSelector selectClasspathResourceAndFilePosition(SelectClasspathResource select) {
		if (select.line() <= 0) {
			return selectClasspathResource(select.value());
		}
		if (select.column() <= 0) {
			return selectClasspathResource(select.value(), FilePosition.from(select.line()));
		}
		return selectClasspathResource(select.value(), FilePosition.from(select.line(), select.column()));
	}

	private FileSelector selectFileAndPosition(SelectFile select) {
		if (select.line() <= 0) {
			return selectFile(select.value());
		}
		if (select.column() <= 0) {
			return selectFile(select.value(), FilePosition.from(select.line()));
		}
		return selectFile(select.value(), FilePosition.from(select.line(), select.column()));
	}

	private static List<UriSelector> selectUris(String[] uris) {
		// @formatter:off
		return Arrays.stream(uris)
				.map(DiscoverySelectors::selectUri)
				.collect(toList());
		// @formatter:on
	}

	private static List<ModuleSelector> selectModules(String[] modules) {
		Set<String> moduleSet = new HashSet<>(Arrays.asList(modules));
		return DiscoverySelectors.selectModules(moduleSet);
	}

	private static List<ClasspathRootSelector> selectClasspathRoots(String[] classpathRoots) {
		// @formatter:off
		Set<Path> classpathRootSet = Arrays.stream(classpathRoots)
				.map(Paths::get)
				.collect(Collectors.toSet());
		// @formatter:on
		return DiscoverySelectors.selectClasspathRoots(classpathRootSet);
	}

	private static List<ClassSelector> selectClasses(Class<?>[] classes) {
		// @formatter:off
		return Arrays.stream(classes)
				.map(DiscoverySelectors::selectClass)
				.collect(toList());
		// @formatter:on
	}

	private static List<PackageSelector> selectPackages(String[] packages) {
		// @formatter:off
		return Arrays.stream(packages)
				.map(DiscoverySelectors::selectPackage)
				.collect(toList());
		// @formatter:on
	}

}
