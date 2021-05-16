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

import static org.junit.platform.commons.util.AnnotationUtils.findAnnotation;
import static org.junit.platform.commons.util.AnnotationUtils.findRepeatableAnnotations;
import static org.junit.platform.engine.discovery.ClassNameFilter.STANDARD_INCLUDE_PATTERN;
import static org.junit.platform.suite.commons.AdditionalDiscoverySelectors.selectClasspathResource;
import static org.junit.platform.suite.commons.AdditionalDiscoverySelectors.selectFile;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.StringUtils;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.Filter;
import org.junit.platform.engine.discovery.ClassNameFilter;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.discovery.PackageNameFilter;
import org.junit.platform.launcher.EngineFilter;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TagFilter;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
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

/**
 * @since 1.8
 */
@API(status = Status.INTERNAL, since = "1.8", consumers = { "org.junit.platform.suite.engine",
		"org.junit.platform.runner" })
public final class SuiteLauncherDiscoveryRequestBuilder {

	private final LauncherDiscoveryRequestBuilder delegate = LauncherDiscoveryRequestBuilder.request();
	private final List<String> selectedClassNames = new ArrayList<>();
	private boolean includeClassNamePatternsUsed;
	private boolean filterStandardClassNamePatterns = false;
	private ConfigurationParameters parentConfigurationParameters;
	private boolean enableParentConfigurationParameters = true;

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

	public SuiteLauncherDiscoveryRequestBuilder selectors(DiscoverySelector... selectors) {
		delegate.selectors(selectors);
		return this;
	}

	public SuiteLauncherDiscoveryRequestBuilder selectors(List<? extends DiscoverySelector> selectors) {
		delegate.selectors(selectors);
		return this;
	}

	public SuiteLauncherDiscoveryRequestBuilder filters(Filter<?>... filters) {
		delegate.filters(filters);
		return this;
	}

	public SuiteLauncherDiscoveryRequestBuilder configurationParameter(String key, String value) {
		delegate.configurationParameter(key, value);
		return this;
	}

	public SuiteLauncherDiscoveryRequestBuilder configurationParameters(Map<String, String> configurationParameters) {
		delegate.configurationParameters(configurationParameters);
		return this;
	}

	public SuiteLauncherDiscoveryRequestBuilder parentConfigurationParameters(
			ConfigurationParameters parentConfigurationParameters) {
		this.parentConfigurationParameters = parentConfigurationParameters;
		return this;
	}

	public SuiteLauncherDiscoveryRequestBuilder enableImplicitConfigurationParameters(boolean enabled) {
		delegate.enableImplicitConfigurationParameters(enabled);
		return this;
	}

	public SuiteLauncherDiscoveryRequestBuilder enableParentConfigurationParameters(boolean enabled) {
		this.enableParentConfigurationParameters = enabled;
		return this;
	}

	public SuiteLauncherDiscoveryRequestBuilder suite(Class<?> suiteClass) {
		Preconditions.notNull(suiteClass, "Suite class must not be null");

		// Annotations in alphabetical order (except @SelectClasses)
		// @formatter:off
		findRepeatableAnnotations(suiteClass, ConfigurationParameter.class)
				.forEach(configuration -> configurationParameter(configuration.key(), configuration.value()));
		findAnnotation(suiteClass, DisableParentConfigurationParameters.class)
				.map(disableParentConfigurationParameters -> false)
				.ifPresent(this::enableParentConfigurationParameters);
		findAnnotationValues(suiteClass, ExcludeClassNamePatterns.class, ExcludeClassNamePatterns::value)
				.flatMap(SuiteLauncherDiscoveryRequestBuilder::trimmed)
				.map(ClassNameFilter::excludeClassNamePatterns)
				.ifPresent(this::filters);
		findAnnotationValues(suiteClass, ExcludeEngines.class, ExcludeEngines::value)
				.map(EngineFilter::excludeEngines)
				.ifPresent(this::filters);
		findAnnotationValues(suiteClass, ExcludePackages.class, ExcludePackages::value)
				.map(PackageNameFilter::excludePackageNames)
				.ifPresent(this::filters);
		findAnnotationValues(suiteClass, ExcludeTags.class, ExcludeTags::value)
				.map(TagFilter::excludeTags)
				.ifPresent(this::filters);
		// Process @SelectClasses before @IncludeClassNamePatterns, since the names
		// of selected classes get automatically added to the include filter.
		findAnnotationValues(suiteClass, SelectClasses.class, SelectClasses::value)
				.map(this::selectClasses)
				.ifPresent(this::selectors);
		findAnnotationValues(suiteClass, IncludeClassNamePatterns.class, IncludeClassNamePatterns::value)
				.flatMap(SuiteLauncherDiscoveryRequestBuilder::trimmed)
				.map(this::createIncludeClassNameFilter)
				.ifPresent(filters -> {
					includeClassNamePatternsUsed = true;
					filters(filters);
				});
		findAnnotationValues(suiteClass, IncludeEngines.class, IncludeEngines::value)
				.map(EngineFilter::includeEngines)
				.ifPresent(this::filters);
		findAnnotationValues(suiteClass, IncludePackages.class, IncludePackages::value)
				.map(PackageNameFilter::includePackageNames)
				.ifPresent(this::filters);
		findAnnotationValues(suiteClass, IncludeTags.class, IncludeTags::value)
				.map(TagFilter::includeTags)
				.ifPresent(this::filters);
		findRepeatableAnnotations(suiteClass, SelectClasspathResource.class)
				.stream()
				.map(annotation -> selectClasspathResource(annotation.value(), annotation.line(), annotation.column()))
				.forEach(this::selectors);
		findAnnotationValues(suiteClass, SelectDirectories.class, SelectDirectories::value)
				.map(AdditionalDiscoverySelectors::selectDirectories)
				.ifPresent(this::selectors);
		findRepeatableAnnotations(suiteClass, SelectFile.class)
				.stream()
				.map(annotation -> selectFile(annotation.value(), annotation.line(), annotation.column()))
				.forEach(this::selectors);
		findAnnotationValues(suiteClass, SelectModules.class, SelectModules::value)
				.map(AdditionalDiscoverySelectors::selectModules)
				.ifPresent(this::selectors);
		findAnnotationValues(suiteClass, SelectUris.class, SelectUris::value)
				.map(AdditionalDiscoverySelectors::selectUris)
				.ifPresent(this::selectors);
		findAnnotationValues(suiteClass, SelectPackages.class, SelectPackages::value)
				.map(AdditionalDiscoverySelectors::selectPackages)
				.ifPresent(this::selectors);
		// @formatter:on
		return this;
	}

	public LauncherDiscoveryRequest build() {
		if (filterStandardClassNamePatterns && !includeClassNamePatternsUsed) {
			delegate.filters(createIncludeClassNameFilter(STANDARD_INCLUDE_PATTERN));
		}

		if (enableParentConfigurationParameters && parentConfigurationParameters != null) {
			delegate.parentConfigurationParameters(parentConfigurationParameters);
		}

		return delegate.build();
	}

	private List<ClassSelector> selectClasses(Class<?>... classes) {
		Arrays.stream(classes).map(Class::getName).distinct().forEach(this.selectedClassNames::add);
		return AdditionalDiscoverySelectors.selectClasses(classes);
	}

	private ClassNameFilter createIncludeClassNameFilter(String... patterns) {
		String[] combinedPatterns = Stream.concat(//
			this.selectedClassNames.stream().map(Pattern::quote), //
			Arrays.stream(patterns)//
		).toArray(String[]::new);
		return ClassNameFilter.includeClassNamePatterns(combinedPatterns);
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
