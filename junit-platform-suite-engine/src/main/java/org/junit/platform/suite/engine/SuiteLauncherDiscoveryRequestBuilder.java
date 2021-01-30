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

import static org.junit.platform.commons.support.AnnotationSupport.findAnnotation;
import static org.junit.platform.commons.support.AnnotationSupport.findRepeatableAnnotations;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClasspathResource;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectFile;
import static org.junit.platform.launcher.TagFilter.includeTags;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.platform.commons.util.StringUtils;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.ClassNameFilter;
import org.junit.platform.engine.discovery.ClasspathResourceSelector;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.engine.discovery.FilePosition;
import org.junit.platform.engine.discovery.FileSelector;
import org.junit.platform.engine.discovery.PackageNameFilter;
import org.junit.platform.launcher.EngineFilter;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TagFilter;
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
import org.junit.platform.suite.api.SelectDirectories;
import org.junit.platform.suite.api.SelectFile;
import org.junit.platform.suite.api.SelectModules;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.SelectUris;

final class SuiteLauncherDiscoveryRequestBuilder {

	private final LauncherDiscoveryRequestBuilder request = LauncherDiscoveryRequestBuilder.request();

	SuiteLauncherDiscoveryRequestBuilder addRequestFrom(UniqueId uniqueId) {
		request.selectors(DiscoverySelectors.selectUniqueId(uniqueId));
		return this;
	}

	SuiteLauncherDiscoveryRequestBuilder addRequestFrom(Class<?> testClass) {
		// Annotations in alphabetical order
		// @formatter:off
		findRepeatableAnnotations(testClass, Configuration.class)
				.forEach(configuration -> request.configurationParameter(configuration.key(), configuration.value()));
		findAnnotation(testClass, ExcludeClassNamePatterns.class)
				.map(ExcludeClassNamePatterns::value)
				.map(SuiteLauncherDiscoveryRequestBuilder::trimmed)
				.map(ClassNameFilter::excludeClassNamePatterns)
				.ifPresent(request::filters);
		findAnnotation(testClass, ExcludeEngines.class).
				map(ExcludeEngines::value)
				.map(EngineFilter::excludeEngines)
				.ifPresent(request::filters);
		findAnnotation(testClass, ExcludePackages.class)
				.map(ExcludePackages::value)
				.map(PackageNameFilter::excludePackageNames)
				.ifPresent(request::filters);
		findAnnotation(testClass, ExcludeTags.class)
				.map(ExcludeTags::value)
				.map(TagFilter::excludeTags)
				.ifPresent(request::filters);
		findAnnotation(testClass, IncludeClassNamePatterns.class)
				.map(IncludeClassNamePatterns::value)
				.map(SuiteLauncherDiscoveryRequestBuilder::trimmed)
				.map(ClassNameFilter::includeClassNamePatterns)
				.ifPresent(request::filters);
		findAnnotation(testClass, IncludeEngines.class)
				.map(IncludeEngines::value)
				.map(EngineFilter::includeEngines)
				.ifPresent(request::filters);
		findAnnotation(testClass, IncludePackages.class)
				.map(IncludePackages::value)
				.map(PackageNameFilter::includePackageNames)
				.ifPresent(request::filters);
		findAnnotation(testClass, IncludeTags.class)
				.map(IncludeTags::value)
				.ifPresent(tagExpressions -> request.filters(includeTags(tagExpressions)));
		findAnnotation(testClass, SelectClasses.class)
				.map(SelectClasses::value)
				.map(Arrays::asList)
				.map(HashSet::new)
				.map(DiscoverySelectors::selectClasses)
				.ifPresent(request::selectors);
		findRepeatableAnnotations(testClass, SelectClasspathResource.class)
				.stream()
				.map(SuiteLauncherDiscoveryRequestBuilder::selectClasspathResourceAndFilePosition)
				.forEach(request::selectors);
		findAnnotation(testClass, SelectDirectories.class)
				.map(SelectDirectories::value)
				.map(DiscoverySelectors::selectDirectory)
				.ifPresent(request::selectors);
		findRepeatableAnnotations(testClass, SelectFile.class)
				.forEach(file -> request.selectors(selectFileAndPosition(file)));
		findAnnotation(testClass, SelectModules.class)
				.map(SelectModules::value)
				.map(Arrays::asList)
				.map(HashSet::new)
				.map(DiscoverySelectors::selectModules)
				.ifPresent(request::selectors);
		findAnnotation(testClass, SelectUris.class)
				.map(SelectUris::value)
				.map(Arrays::asList)
				.map(DiscoverySelectors::selectUris)
				.ifPresent(request::selectors);
		findAnnotation(testClass, SelectPackages.class)
				.map(SelectPackages::value)
				.map(Arrays::asList)
				.map(HashSet::new)
				.map(DiscoverySelectors::selectPackages)
				.ifPresent(request::selectors);
		// @formatter:on
		return this;
	}

	LauncherDiscoveryRequest build() {
		return request.build();
	}

	private static ClasspathResourceSelector selectClasspathResourceAndFilePosition(SelectClasspathResource select) {
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

	private static String[] trimmed(String[] patterns) {
		if (patterns.length == 0) {
			return patterns;
		}
		// @formatter:off
		return Arrays.stream(patterns)
				.filter(StringUtils::isNotBlank)
				.map(String::trim)
				.toArray(String[]::new);
		// @formatter:on
	}

}
