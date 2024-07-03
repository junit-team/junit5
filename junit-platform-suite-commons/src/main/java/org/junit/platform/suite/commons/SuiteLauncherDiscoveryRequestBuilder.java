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
import org.junit.platform.suite.api.ConfigurationParametersResource;
import org.junit.platform.suite.api.DisableParentConfigurationParameters;
import org.junit.platform.suite.api.ExcludeClassNamePatterns;
import org.junit.platform.suite.api.ExcludeEngines;
import org.junit.platform.suite.api.ExcludePackages;
import org.junit.platform.suite.api.ExcludeTags;
import org.junit.platform.suite.api.IncludeClassNamePatterns;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.IncludePackages;
import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.Select;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.SelectDirectories;
import org.junit.platform.suite.api.SelectFile;
import org.junit.platform.suite.api.SelectMethod;
import org.junit.platform.suite.api.SelectModules;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.SelectUris;

/**
 * The {@code SuiteLauncherDiscoveryRequestBuilder} provides a light-weight DSL
 * for generating a {@link LauncherDiscoveryRequest} specifically tailored for
 * suite execution.
 *
 * <h2>Example</h2>
 *
 * <pre>{@code
 * SuiteLauncherDiscoveryRequestBuilder.request()
 *   .selectors(
 *        selectPackage("org.example.user"),
 *        selectClass("org.example.payment.PaymentTests"),
 *        selectClass(ShippingTests.class),
 *        selectMethod("org.example.order.OrderTests#test1"),
 *        selectMethod("org.example.order.OrderTests#test2()"),
 *        selectMethod("org.example.order.OrderTests#test3(java.lang.String)"),
 *        selectMethod("org.example.order.OrderTests", "test4"),
 *        selectMethod(OrderTests.class, "test5"),
 *        selectMethod(OrderTests.class, testMethod),
 *        selectClasspathRoots(Collections.singleton(Paths.get("/my/local/path1"))),
 *        selectUniqueId("unique-id-1"),
 *        selectUniqueId("unique-id-2")
 *   )
 *   .filters(
 *        includeEngines("junit-jupiter", "spek"),
 *        // excludeEngines("junit-vintage"),
 *        includeTags("fast"),
 *        // excludeTags("slow"),
 *        includeClassNamePatterns(".*Test[s]?")
 *        // includeClassNamePatterns("org\.example\.tests.*")
 *   )
 *   .configurationParameter("key", "value")
 *   .enableImplicitConfigurationParameters(true)
 *   .applyConfigurationParametersFromSuite(MySuite.class)
 *   .applySelectorsAndFiltersFromSuite(MySuite.class)
 *   .build();
 * }</pre>
 *
 * @since 1.8
 * @see org.junit.platform.engine.discovery.DiscoverySelectors
 * @see org.junit.platform.engine.discovery.ClassNameFilter
 * @see org.junit.platform.launcher.EngineFilter
 * @see org.junit.platform.launcher.TagFilter
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

	/**
	 * Create a new {@code SuiteLauncherDiscoveryRequestBuilder}.
	 *
	 * @return a new builder
	 */
	public static SuiteLauncherDiscoveryRequestBuilder request() {
		return new SuiteLauncherDiscoveryRequestBuilder();
	}

	/**
	 * Add all supplied {@code selectors} to the request.
	 *
	 * @param selectors the {@code DiscoverySelectors} to add; never {@code null}
	 * @return this builder for method chaining
	 */
	public SuiteLauncherDiscoveryRequestBuilder selectors(DiscoverySelector... selectors) {
		this.delegate.selectors(selectors);
		return this;
	}

	/**
	 * Add all supplied {@code selectors} to the request.
	 *
	 * @param selectors the {@code DiscoverySelectors} to add; never {@code null}
	 * @return this builder for method chaining
	 */
	public SuiteLauncherDiscoveryRequestBuilder selectors(List<? extends DiscoverySelector> selectors) {
		this.delegate.selectors(selectors);
		return this;
	}

	/**
	 * Add all supplied {@code filters} to the request.
	 *
	 * <p>The {@code filters} are combined using AND semantics, i.e. all of them
	 * have to include a resource for it to end up in the test plan.
	 *
	 * <p><strong>Warning</strong>: be cautious when registering multiple competing
	 * {@link EngineFilter#includeEngines include} {@code EngineFilters} or multiple
	 * competing {@link EngineFilter#excludeEngines exclude} {@code EngineFilters}
	 * for the same discovery request since doing so will likely lead to
	 * undesirable results (i.e., zero engines being active).
	 *
	 * @param filters the {@code Filter}s to add; never {@code null}
	 * @return this builder for method chaining
	 */
	public SuiteLauncherDiscoveryRequestBuilder filters(Filter<?>... filters) {
		this.delegate.filters(filters);
		return this;
	}

	/**
	 * Specify whether to filter standard class name patterns.
	 * <p>If set to {@code true}, standard class name patterns are filtered.
	 *
	 * @param filterStandardClassNamePatterns {@code true} to filter standard class
	 * name patterns, {@code false} otherwise
	 * @return this builder for method chaining
	 */
	public SuiteLauncherDiscoveryRequestBuilder filterStandardClassNamePatterns(
			boolean filterStandardClassNamePatterns) {
		this.filterStandardClassNamePatterns = filterStandardClassNamePatterns;
		return this;
	}

	/**
	 * Add the supplied <em>configuration parameter</em> to the request.
	 *
	 * @param key the configuration parameter key under which to store the
	 * value; never {@code null} or blank
	 * @param value the value to store
	 * @return this builder for method chaining
	 */
	public SuiteLauncherDiscoveryRequestBuilder configurationParameter(String key, String value) {
		this.delegate.configurationParameter(key, value);
		return this;
	}

	/**
	 * Add all supplied configuration parameters to the request.
	 *
	 * @param configurationParameters the map of configuration parameters to add;
	 * never {@code null}
	 * @return this builder for method chaining
	 * @see #configurationParameter(String, String)
	 */
	public SuiteLauncherDiscoveryRequestBuilder configurationParameters(Map<String, String> configurationParameters) {
		this.delegate.configurationParameters(configurationParameters);
		return this;
	}

	public SuiteLauncherDiscoveryRequestBuilder configurationParametersResource(String resourceFile) {
		this.delegate.configurationParametersResources(resourceFile);
		return this;
	}

	/**
	 * Set the parent configuration parameters to use for the request.
	 *
	 * <p>Any explicit configuration parameters configured via
	 * {@link #configurationParameter(String, String)} or
	 * {@link #configurationParameters(Map)} takes precedence over the supplied
	 * configuration parameters.
	 *
	 * @param parentConfigurationParameters the parent instance to use for looking
	 * up configuration parameters that have not been explicitly configured;
	 * never {@code null}
	 * @return this builder for method chaining
	 * @see #configurationParameter(String, String)
	 * @see #configurationParameters(Map)
	 */
	public SuiteLauncherDiscoveryRequestBuilder parentConfigurationParameters(
			ConfigurationParameters parentConfigurationParameters) {
		this.parentConfigurationParameters = parentConfigurationParameters;
		return this;
	}

	/**
	 * Configure whether implicit configuration parameters should be considered.
	 *
	 * <p>By default, in addition to those parameters that are passed explicitly
	 * to this builder, configuration parameters are read from system properties
	 * and from the {@code junit-platform.properties} classpath resource.
	 * Passing {@code false} to this method, disables the latter two sources so
	 * that only explicit configuration parameters are taken into account.
	 *
	 * @param enabled {@code true} if implicit configuration parameters should be
	 * considered
	 * @return this builder for method chaining
	 * @see #configurationParameter(String, String)
	 * @see #configurationParameters(Map)
	 */
	public SuiteLauncherDiscoveryRequestBuilder enableImplicitConfigurationParameters(boolean enabled) {
		this.delegate.enableImplicitConfigurationParameters(enabled);
		return this;
	}

	/**
	 * Apply a suite's annotation-based configuration, selectors, and filters to
	 * this builder.
	 *
	 * @param suiteClass the class to apply the annotations from; never {@code null}
	 * @return this builder for method chaining
	 * @see org.junit.platform.suite.api.Suite
	 * @deprecated as of JUnit Platform 1.11 in favor of
	 * {@link #applyConfigurationParametersFromSuite} and
	 * {@link #applySelectorsAndFiltersFromSuite}
	 */
	@Deprecated
	public SuiteLauncherDiscoveryRequestBuilder suite(Class<?> suiteClass) {
		Preconditions.notNull(suiteClass, "Suite class must not be null");
		applyConfigurationParametersFromSuite(suiteClass);
		applySelectorsAndFiltersFromSuite(suiteClass);
		return this;
	}

	/**
	 * Apply a suite's annotation-based configuration to this builder.
	 *
	 * <p>This will apply the configuration from the following annotations.
	 * <ul>
	 *   <li>{@link ConfigurationParameter}</li>
	 *   <li>{@link DisableParentConfigurationParameters}</li>
	 * </ul>
	 *
	 * @param suiteClass the class to apply the configuration annotations from;
	 * never {@code null}
	 * @return this builder for method chaining
	 * @since 1.11
	 * @see org.junit.platform.suite.api.Suite
	 */
	public SuiteLauncherDiscoveryRequestBuilder applyConfigurationParametersFromSuite(Class<?> suiteClass) {
		Preconditions.notNull(suiteClass, "Suite class must not be null");

		// @formatter:off
		findRepeatableAnnotations(suiteClass, ConfigurationParameter.class)
				.forEach(configuration -> configurationParameter(configuration.key(), configuration.value()));
		findRepeatableAnnotations(suiteClass, ConfigurationParametersResource.class)
				.forEach(configResource -> configurationParametersResource(configResource.value()));
		findAnnotation(suiteClass, DisableParentConfigurationParameters.class)
				.ifPresent(__ -> this.enableParentConfigurationParameters = false);
		// @formatter:on
		return this;
	}

	/**
	 * Apply a suite's annotation-based discovery selectors and filters to this
	 * builder.
	 *
	 * <p>This will apply the configuration from the following annotations.
	 * <ul>
	 *   <li>{@link ExcludeClassNamePatterns}</li>
	 *   <li>{@link ExcludeEngines}</li>
	 *   <li>{@link ExcludePackages}</li>
	 *   <li>{@link ExcludeTags}</li>
	 *   <li>{@link IncludeClassNamePatterns}</li>
	 *   <li>{@link IncludeEngines}</li>
	 *   <li>{@link IncludePackages}</li>
	 *   <li>{@link IncludeTags}</li>
	 *   <li>{@link SelectClasses}</li>
	 *   <li>{@link SelectClasspathResource}</li>
	 *   <li>{@link SelectDirectories}</li>
	 *   <li>{@link SelectFile}</li>
	 *   <li>{@link SelectMethod}</li>
	 *   <li>{@link SelectModules}</li>
	 *   <li>{@link SelectUris}</li>
	 *   <li>{@link SelectPackages}</li>
	 * </ul>
	 *
	 * @param suiteClass the class to apply the discovery selectors and filter
	 * annotations from; never {@code null}
	 * @return this builder for method chaining
	 * @since 1.11
	 * @see org.junit.platform.suite.api.Suite
	 */
	public SuiteLauncherDiscoveryRequestBuilder applySelectorsAndFiltersFromSuite(Class<?> suiteClass) {
		Preconditions.notNull(suiteClass, "Suite class must not be null");

		// Annotations in alphabetical order (except @SelectClasses)
		// @formatter:off
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
					this.includeClassNamePatternsUsed = true;
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
		findAnnotationValues(suiteClass, Select.class, Select::value)
				.map(AdditionalDiscoverySelectors::parseIdentifiers)
				.ifPresent(this::selectors);
		// @formatter:on
		return this;
	}

	/**
	 * Build the {@link LauncherDiscoveryRequest} that has been configured via
	 * this builder.
	 */
	public LauncherDiscoveryRequest build() {
		if (this.filterStandardClassNamePatterns && !this.includeClassNamePatternsUsed) {
			this.delegate.filters(createIncludeClassNameFilter(STANDARD_INCLUDE_PATTERN));
		}

		if (this.enableParentConfigurationParameters && this.parentConfigurationParameters != null) {
			this.delegate.parentConfigurationParameters(this.parentConfigurationParameters);
		}

		return this.delegate.build();
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
		this.selectedClassNames.add(methodSelector.getClassName());
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
