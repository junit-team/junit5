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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.StringUtils;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.Filter;
import org.junit.platform.engine.discovery.ClassNameFilter;
import org.junit.platform.engine.discovery.PackageNameFilter;
import org.junit.platform.launcher.EngineFilter;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TagFilter;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.suite.api.ConfigurationParameter;
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

/**
 * @since 1.8
 */
@API(status = Status.INTERNAL, since = "1.8", consumers = { "org.junit.platform.suite.engine",
		"org.junit.platform.runner" })
public final class SuiteLauncherDiscoveryRequestBuilder {

	private final LauncherDiscoveryRequestBuilder delegate = LauncherDiscoveryRequestBuilder.request();
	private boolean includeClassNamePatternsUsed;
	private boolean filterStandardClassNamePatterns = false;

	private SuiteLauncherDiscoveryRequestBuilder() {
	}

	public static SuiteLauncherDiscoveryRequestBuilder request() {
		return new SuiteLauncherDiscoveryRequestBuilder();
	}

	public SuiteLauncherDiscoveryRequestBuilder filterStandardClassNamePatterns(
			boolean filterStandardClassNamePatterns) {
		this.filterStandardClassNamePatterns = filterStandardClassNamePatterns;
		return this;
	}

	public LauncherDiscoveryRequestBuilder selectors(DiscoverySelector... selectors) {
		return delegate.selectors(selectors);
	}

	public LauncherDiscoveryRequestBuilder selectors(List<? extends DiscoverySelector> selectors) {
		return delegate.selectors(selectors);
	}

	public LauncherDiscoveryRequestBuilder filters(Filter<?>... filters) {
		return delegate.filters(filters);
	}

	public LauncherDiscoveryRequestBuilder configurationParameter(String key, String value) {
		return delegate.configurationParameter(key, value);
	}

	public LauncherDiscoveryRequestBuilder configurationParameters(Map<String, String> configurationParameters) {
		return delegate.configurationParameters(configurationParameters);
	}

	public SuiteLauncherDiscoveryRequestBuilder suite(AnnotatedElement suite) {
		Preconditions.notNull(suite, "Test class must not be null");

		// Annotations in alphabetical order
		// @formatter:off
		findRepeatableAnnotations(suite, ConfigurationParameter.class)
				.forEach(configuration -> configurationParameter(configuration.key(), configuration.value()));
		findAnnotationValues(suite, ExcludeClassNamePatterns.class, ExcludeClassNamePatterns::value)
				.flatMap(SuiteLauncherDiscoveryRequestBuilder::trimmed)
				.map(ClassNameFilter::excludeClassNamePatterns)
				.ifPresent(this::filters);
		findAnnotationValues(suite, ExcludeEngines.class, ExcludeEngines::value)
				.map(EngineFilter::excludeEngines)
				.ifPresent(this::filters);
		findAnnotationValues(suite, ExcludePackages.class, ExcludePackages::value)
				.map(PackageNameFilter::excludePackageNames)
				.ifPresent(this::filters);
		findAnnotationValues(suite, ExcludeTags.class, ExcludeTags::value)
				.map(TagFilter::excludeTags)
				.ifPresent(this::filters);
		findAnnotationValues(suite, IncludeClassNamePatterns.class, IncludeClassNamePatterns::value)
				.flatMap(SuiteLauncherDiscoveryRequestBuilder::trimmed)
				.map(ClassNameFilter::includeClassNamePatterns)
				.ifPresent(filters -> {
					includeClassNamePatternsUsed = true;
					filters(filters);
				});
		findAnnotationValues(suite, IncludeEngines.class, IncludeEngines::value)
				.map(EngineFilter::includeEngines)
				.ifPresent(this::filters);
		findAnnotationValues(suite, IncludePackages.class, IncludePackages::value)
				.map(PackageNameFilter::includePackageNames)
				.ifPresent(this::filters);
		findAnnotationValues(suite, IncludeTags.class, IncludeTags::value)
				.map(TagFilter::includeTags)
				.ifPresent(this::filters);
		findAnnotationValues(suite, SelectClasses.class, SelectClasses::value)
				.map(AdditionalDiscoverySelectors::selectClasses)
				.ifPresent(this::selectors);
		findRepeatableAnnotations(suite, SelectClasspathResource.class)
				.stream()
				.map(annotation -> selectClasspathResource(annotation.value(), annotation.line(), annotation.column()))
				.forEach(this::selectors);
		findAnnotationValues(suite, SelectDirectories.class, SelectDirectories::value)
				.map(AdditionalDiscoverySelectors::selectDirectories)
				.ifPresent(this::selectors);
		findRepeatableAnnotations(suite, SelectFile.class)
				.stream()
				.map(annotation -> selectFile(annotation.value(), annotation.line(), annotation.column()))
				.forEach(this::selectors);
		findAnnotationValues(suite, SelectModules.class, SelectModules::value)
				.map(AdditionalDiscoverySelectors::selectModules)
				.ifPresent(this::selectors);
		findAnnotationValues(suite, SelectUris.class, SelectUris::value)
				.map(AdditionalDiscoverySelectors::selectUris)
				.ifPresent(this::selectors);
		findAnnotationValues(suite, SelectPackages.class, SelectPackages::value)
				.map(AdditionalDiscoverySelectors::selectPackages)
				.ifPresent(this::selectors);
		// @formatter:on
		return this;
	}

	public LauncherDiscoveryRequest build() {
		if (filterStandardClassNamePatterns && !includeClassNamePatternsUsed) {
			delegate.filters(ClassNameFilter.includeClassNamePatterns(STANDARD_INCLUDE_PATTERN));
		}
		return delegate.build();
	}

	private static <A extends Annotation, V> Optional<V[]> findAnnotationValues(AnnotatedElement element,
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
