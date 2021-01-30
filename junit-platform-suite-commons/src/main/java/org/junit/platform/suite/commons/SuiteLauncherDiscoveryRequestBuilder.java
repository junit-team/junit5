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

import static org.junit.platform.commons.support.AnnotationSupport.findAnnotation;
import static org.junit.platform.commons.support.AnnotationSupport.findRepeatableAnnotations;
import static org.junit.platform.engine.discovery.ClassNameFilter.STANDARD_INCLUDE_PATTERN;
import static org.junit.platform.suite.commons.AdditionalDiscoverySelectors.selectClasspathResource;
import static org.junit.platform.suite.commons.AdditionalDiscoverySelectors.selectFile;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.StringUtils;
import org.junit.platform.engine.discovery.ClassNameFilter;
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

@API(status = Status.INTERNAL, since = "1.8", consumers = { "org.junit.platform.suite.engine",
		"org.junit.platform.runner" })
public final class SuiteLauncherDiscoveryRequestBuilder {

	private final LauncherDiscoveryRequestBuilder request;
	private boolean classNamePatternsIncluded;
	private boolean includeStandardClassNamePatternsIfNotPresent = true;

	private SuiteLauncherDiscoveryRequestBuilder(LauncherDiscoveryRequestBuilder request) {
		this.request = request;
	}

	public static SuiteLauncherDiscoveryRequestBuilder request(LauncherDiscoveryRequestBuilder request) {
		Preconditions.notNull(request, "Launcher discovery request builder must not be null");
		return new SuiteLauncherDiscoveryRequestBuilder(request);
	}

	public SuiteLauncherDiscoveryRequestBuilder includeStandardClassNamePatternsIfNotPresent(
			boolean includeStandardClassNamePatternsIfNotPresent) {
		this.includeStandardClassNamePatternsIfNotPresent = includeStandardClassNamePatternsIfNotPresent;
		return this;
	}

	public SuiteLauncherDiscoveryRequestBuilder suite(Class<?> testClass) {
		// Annotations in alphabetical order
		// @formatter:off
		findRepeatableAnnotations(testClass, Configuration.class)
				.forEach(configuration -> request.configurationParameter(configuration.key(), configuration.value()));
		findAnnotationValues(testClass, ExcludeClassNamePatterns.class, ExcludeClassNamePatterns::value)
				.flatMap(SuiteLauncherDiscoveryRequestBuilder::trimmed)
				.map(ClassNameFilter::excludeClassNamePatterns)
				.ifPresent(request::filters);
		findAnnotationValues(testClass, ExcludeEngines.class, ExcludeEngines::value)
				.map(EngineFilter::excludeEngines)
				.ifPresent(request::filters);
		findAnnotationValues(testClass, ExcludePackages.class, ExcludePackages::value)
				.map(PackageNameFilter::excludePackageNames)
				.ifPresent(request::filters);
		findAnnotationValues(testClass, ExcludeTags.class, ExcludeTags::value)
				.map(TagFilter::excludeTags)
				.ifPresent(request::filters);
		findAnnotationValues(testClass, IncludeClassNamePatterns.class, IncludeClassNamePatterns::value)
				.flatMap(SuiteLauncherDiscoveryRequestBuilder::trimmed)
				.map(ClassNameFilter::includeClassNamePatterns)
				.ifPresent(filters ->{
					classNamePatternsIncluded = true;
					request.filters(filters);
				});
		findAnnotationValues(testClass, IncludeEngines.class, IncludeEngines::value)
				.map(EngineFilter::includeEngines)
				.ifPresent(request::filters);
		findAnnotationValues(testClass, IncludePackages.class, IncludePackages::value)
				.map(PackageNameFilter::includePackageNames)
				.ifPresent(request::filters);
		findAnnotationValues(testClass, IncludeTags.class, IncludeTags::value)
				.map(TagFilter::includeTags)
				.ifPresent(request::filters);
		findAnnotationValues(testClass, SelectClasses.class, SelectClasses::value)
				.map(AdditionalDiscoverySelectors::selectClasses)
				.ifPresent(request::selectors);
		findRepeatableAnnotations(testClass, SelectClasspathResource.class)
				.stream()
				.map(annotation -> selectClasspathResource(annotation.value(), annotation.line(), annotation.column()))
				.forEach(request::selectors);
		findAnnotationValues(testClass, SelectDirectories.class, SelectDirectories::value)
				.map(AdditionalDiscoverySelectors::selectDirectories)
				.ifPresent(request::selectors);
		findRepeatableAnnotations(testClass, SelectFile.class)
				.stream()
				.map(annotation -> selectFile(annotation.value(), annotation.line(), annotation.column()))
				.forEach(request::selectors);
		findAnnotationValues(testClass, SelectModules.class, SelectModules::value)
				.map(AdditionalDiscoverySelectors::selectModules)
				.ifPresent(request::selectors);
		findAnnotationValues(testClass, SelectUris.class, SelectUris::value)
				.map(AdditionalDiscoverySelectors::selectUris)
				.ifPresent(request::selectors);
		findAnnotationValues(testClass, SelectPackages.class, SelectPackages::value)
				.map(AdditionalDiscoverySelectors::selectPackages)
				.ifPresent(request::selectors);
		// @formatter:on
		return this;
	}

	public LauncherDiscoveryRequest build() {
		if (includeStandardClassNamePatternsIfNotPresent && !classNamePatternsIncluded) {
			request.filters(ClassNameFilter.includeClassNamePatterns(STANDARD_INCLUDE_PATTERN));
		}
		return request.build();
	}

	public static <A extends Annotation, V> Optional<V[]> findAnnotationValues(AnnotatedElement element,
			Class<A> annotationType, Function<A, V[]> valueExtractor) {
		return findAnnotation(element, annotationType).map(valueExtractor).filter(values -> values.length > 0);
	}

	private static Optional<String[]> trimmed(String[] patterns) {
		if (patterns.length == 0) {
			return Optional.empty();
		}
		// @formatter:off
		return Optional.of(Arrays.stream(patterns)
				.filter(StringUtils::isNotBlank)
				.map(String::trim)
				.toArray(String[]::new));
		// @formatter:on
	}

}
