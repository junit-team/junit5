/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.suite.commons;

import static java.util.stream.Collectors.toList;
import static org.junit.platform.commons.util.AnnotationUtils.findAnnotation;
import static org.junit.platform.commons.util.AnnotationUtils.findRepeatableAnnotations;
import static org.junit.platform.engine.discovery.ClassNameFilter.STANDARD_INCLUDE_PATTERN;
import static org.junit.platform.suite.commons.AdditionalDiscoverySelectors.selectClasspathResource;
import static org.junit.platform.suite.commons.AdditionalDiscoverySelectors.selectFile;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.engine.discovery.MethodSelector;
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
import org.junit.platform.suite.api.SelectMethod;
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
	private final Set<String> selectedClassNames = new LinkedHashSet<>();
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

	public SuiteLauncherDiscoveryRequestBuilder suite(Class<?> suiteClass) {
		Preconditions.notNull(suiteClass, "Suite class must not be null");

		// Annotations in alphabetical order (except @SelectClasses)
		// @formatter:off
		findRepeatableAnnotations(suiteClass, ConfigurationParameter.class)
				.forEach(configuration -> configurationParameter(configuration.key(), configuration.value()));
		findAnnotation(suiteClass, DisableParentConfigurationParameters.class)
				.ifPresent(__ -> enableParentConfigurationParameters = false);
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
		findAnnotation(suiteClass, SelectClasses.class)
				.map(annotation -> selectClasses(suiteClass, annotation))
				.ifPresent(this::selectors);
		findRepeatableAnnotations(suiteClass, SelectMethod.class)
				.stream()
				.map(annotation -> selectMethod(suiteClass, annotation))
				.forEach(this::selectors);
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

	private List<ClassSelector> selectClasses(Class<?> suiteClass, SelectClasses annotation) {
		return toClassSelectors(suiteClass, annotation) //
				.distinct() //
				.peek(selector -> this.selectedClassNames.add(selector.getClassName())) //
				.collect(toList());
	}

	private static Stream<ClassSelector> toClassSelectors(Class<?> suiteClass, SelectClasses annotation) {
		Preconditions.condition(annotation.value().length > 0 || annotation.names().length > 0,
			() -> String.format("@SelectClasses on class [%s] must declare at least one class reference or name",
				suiteClass.getName()));
		return Stream.concat(//
			AdditionalDiscoverySelectors.selectClasses(annotation.value()), //
			AdditionalDiscoverySelectors.selectClasses(annotation.names()) //
		);
	}

	private MethodSelector selectMethod(Class<?> suiteClass, SelectMethod annotation) {
		MethodSelector methodSelector = toMethodSelector(suiteClass, annotation);
		selectedClassNames.add(methodSelector.getClassName());
		return methodSelector;
	}

	private MethodSelector toMethodSelector(Class<?> suiteClass, SelectMethod annotation) {
		if (!annotation.value().isEmpty()) {
			Preconditions.condition(annotation.type() == Class.class,
				() -> prefixErrorMessageForInvalidSelectMethodUsage(suiteClass,
					"type must not be set in conjunction with fully qualified method name"));
			Preconditions.condition(annotation.typeName().isEmpty(),
				() -> prefixErrorMessageForInvalidSelectMethodUsage(suiteClass,
					"type name must not be set in conjunction with fully qualified method name"));
			Preconditions.condition(annotation.name().isEmpty(),
				() -> prefixErrorMessageForInvalidSelectMethodUsage(suiteClass,
					"method name must not be set in conjunction with fully qualified method name"));
			Preconditions.condition(annotation.parameterTypes().length == 0,
				() -> prefixErrorMessageForInvalidSelectMethodUsage(suiteClass,
					"parameter types must not be set in conjunction with fully qualified method name"));
			Preconditions.condition(annotation.parameterTypeNames().isEmpty(),
				() -> prefixErrorMessageForInvalidSelectMethodUsage(suiteClass,
					"parameter type names must not be set in conjunction with fully qualified method name"));

			return DiscoverySelectors.selectMethod(annotation.value());
		}

		Class<?> type = annotation.type() == Class.class ? null : annotation.type();
		String typeName = annotation.typeName().isEmpty() ? null : annotation.typeName().trim();
		String methodName = Preconditions.notBlank(annotation.name(),
			() -> prefixErrorMessageForInvalidSelectMethodUsage(suiteClass, "method name must not be blank"));
		Class<?>[] parameterTypes = annotation.parameterTypes().length == 0 ? null : annotation.parameterTypes();
		String parameterTypeNames = annotation.parameterTypeNames().trim();
		if (parameterTypes != null) {
			Preconditions.condition(parameterTypeNames.isEmpty(),
				() -> prefixErrorMessageForInvalidSelectMethodUsage(suiteClass,
					"either parameter type names or parameter types must be set but not both"));
		}
		if (type == null) {
			Preconditions.notBlank(typeName, () -> prefixErrorMessageForInvalidSelectMethodUsage(suiteClass,
				"type must be set or type name must not be blank"));
			if (parameterTypes == null) {
				return DiscoverySelectors.selectMethod(typeName, methodName, parameterTypeNames);
			}
			else {
				return DiscoverySelectors.selectMethod(typeName, methodName, parameterTypes);
			}
		}
		else {
			Preconditions.condition(typeName == null, () -> prefixErrorMessageForInvalidSelectMethodUsage(suiteClass,
				"either type name or type must be set but not both"));
			if (parameterTypes == null) {
				return DiscoverySelectors.selectMethod(type, methodName, parameterTypeNames);
			}
			else {
				return DiscoverySelectors.selectMethod(type, methodName, parameterTypes);
			}
		}
	}

	private static String prefixErrorMessageForInvalidSelectMethodUsage(Class<?> suiteClass, String detailMessage) {
		return String.format("@SelectMethod on class [%s]: %s", suiteClass.getName(), detailMessage);
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
