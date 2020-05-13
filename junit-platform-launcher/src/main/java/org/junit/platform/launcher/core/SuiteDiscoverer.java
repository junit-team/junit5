/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher.core;

import static java.util.stream.Collectors.toList;
import static org.junit.platform.commons.support.AnnotationSupport.findAnnotation;
import static org.junit.platform.commons.support.AnnotationSupport.findRepeatableAnnotations;
import static org.junit.platform.commons.util.ReflectionUtils.findAllClassesInClasspathRoot;
import static org.junit.platform.commons.util.ReflectionUtils.findAllClassesInModule;
import static org.junit.platform.commons.util.ReflectionUtils.findAllClassesInPackage;
import static org.junit.platform.commons.util.ReflectionUtils.isAbstract;
import static org.junit.platform.commons.util.ReflectionUtils.isInnerClass;
import static org.junit.platform.commons.util.ReflectionUtils.isPrivate;
import static org.junit.platform.engine.Filter.composeFilters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import org.junit.platform.commons.support.AnnotationSupport;
import org.junit.platform.commons.util.ClassFilter;
import org.junit.platform.engine.DiscoveryFilter;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.discovery.ClassNameFilter;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.discovery.ClasspathRootSelector;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.engine.discovery.ModuleSelector;
import org.junit.platform.engine.discovery.PackageNameFilter;
import org.junit.platform.engine.discovery.PackageSelector;
import org.junit.platform.launcher.EngineFilter;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TagFilter;
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
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

class SuiteDiscoverer {

	public List<LauncherDiscoveryRequest> resolve(LauncherDiscoveryRequest request) {
		SuiteClassResolver classResolver = new SuiteClassResolver(request);
		ClassFilter classFilter = buildClassFilter(request, SuiteDiscoverer::isSuiteClass);
		request.getSelectorsByType(ClasspathRootSelector.class).forEach(selector -> {
			findAllClassesInClasspathRoot(selector.getClasspathRoot(), classFilter).forEach(
				classResolver::resolveClass);
		});
		request.getSelectorsByType(ModuleSelector.class).forEach(selector -> {
			findAllClassesInModule(selector.getModuleName(), classFilter).forEach(classResolver::resolveClass);
		});
		request.getSelectorsByType(PackageSelector.class).forEach(selector -> {
			findAllClassesInPackage(selector.getPackageName(), classFilter).forEach(classResolver::resolveClass);
		});
		request.getSelectorsByType(ClassSelector.class).forEach(selector -> {
			classResolver.resolveClass(selector.getJavaClass());
		});
		return classResolver.requests;
	}

	// start TODO
	// use org.junit.platform.engine.support.discovery.EngineDiscoveryRequestResolver instead of
	// copied (deprecated) utility methods from org.junit.platform.engine.support.filter.ClasspathScanningSupport

	/**
	 * Build a {@link Predicate} for fully qualified class names to be used for
	 * classpath scanning from an {@link EngineDiscoveryRequest}.
	 *
	 * @param request the request to build a predicate from
	 */
	public static Predicate<String> buildClassNamePredicate(EngineDiscoveryRequest request) {
		List<DiscoveryFilter<String>> filters = new ArrayList<>();
		filters.addAll(request.getFiltersByType(ClassNameFilter.class));
		filters.addAll(request.getFiltersByType(PackageNameFilter.class));
		return composeFilters(filters).toPredicate();
	}

	/**
	 * Build a {@link ClassFilter} by combining the name predicate built by
	 * {@link #buildClassNamePredicate(EngineDiscoveryRequest)} and the passed-in
	 * class predicate.
	 *
	 * @param request the request to build a name predicate from
	 * @param classPredicate the class predicate
	 */
	public static ClassFilter buildClassFilter(EngineDiscoveryRequest request, Predicate<Class<?>> classPredicate) {
		return ClassFilter.of(buildClassNamePredicate(request), classPredicate);
	}

	// end TODO

	private static boolean isSuiteClass(Class<?> candidate) {
		// Please do not collapse the following into a single statement.
		// TODO duplication with IsPotentialTestContainer
		if (isPrivate(candidate)) {
			return false;
		}
		if (isAbstract(candidate)) {
			return false;
		}
		if (candidate.isLocalClass()) {
			return false;
		}
		if (candidate.isAnonymousClass()) {
			return false;
		}
		if (isInnerClass(candidate)) {
			return false;
		}
		return AnnotationSupport.isAnnotated(candidate, Suite.class);
	}

	static class SuiteClassResolver {
		private final List<LauncherDiscoveryRequest> requests = new ArrayList<>();
		private final LauncherDiscoveryRequest originalRequest;

		SuiteClassResolver(LauncherDiscoveryRequest originalRequest) {
			this.originalRequest = originalRequest;
		}

		void resolveClass(Class<?> javaClass) {
			if (isSuiteClass(javaClass)) {
				requests.add(toRequest(javaClass));
			}
		}

		private LauncherDiscoveryRequest toRequest(Class<?> javaClass) {
			LauncherDiscoveryRequestBuilder request = LauncherDiscoveryRequestBuilder.request();
			request.parent(new SuiteDescriptor(javaClass));
			// TODO Decide which parameters to copy from original request
			LauncherConfigurationParameters originalConfigurationParameters = (LauncherConfigurationParameters) originalRequest.getConfigurationParameters();
			request.configurationParameters(originalConfigurationParameters.getExplicitParameters());
			// @formatter:off
            findAnnotation(javaClass, SelectClasses.class).map(SelectClasses::value)
                    .ifPresent(classes -> request.selectors(selectClasses(classes)));
            findAnnotation(javaClass, SelectPackages.class).map(SelectPackages::value)
                    .ifPresent(packages -> request.selectors(selectPackages(packages)));
            findAnnotation(javaClass, ExcludeClassNamePatterns.class).map(ExcludeClassNamePatterns::value)
                    .ifPresent(patterns -> request.filters(ClassNameFilter.excludeClassNamePatterns(patterns)));
            findAnnotation(javaClass, IncludeClassNamePatterns.class).map(IncludeClassNamePatterns::value)
                    .ifPresent(patterns -> request.filters(ClassNameFilter.includeClassNamePatterns(patterns)));
            findAnnotation(javaClass, ExcludeEngines.class).map(ExcludeEngines::value)
                    .ifPresent(engineIds -> request.filters(EngineFilter.excludeEngines(engineIds)));
            findAnnotation(javaClass, IncludeEngines.class).map(IncludeEngines::value)
                    .ifPresent(engineIds -> request.filters(EngineFilter.includeEngines(engineIds)));
            findAnnotation(javaClass, ExcludePackages.class).map(ExcludePackages::value)
                    .ifPresent(packageNames -> request.filters(PackageNameFilter.excludePackageNames(packageNames)));
            findAnnotation(javaClass, IncludePackages.class).map(IncludePackages::value)
                    .ifPresent(packageNames -> request.filters(PackageNameFilter.includePackageNames(packageNames)));
            findAnnotation(javaClass, ExcludeTags.class).map(ExcludeTags::value)
                    .ifPresent(tagExpressions -> request.filters(TagFilter.excludeTags(tagExpressions)));
            findAnnotation(javaClass, IncludeTags.class).map(IncludeTags::value)
                    .ifPresent(tagExpressions -> request.filters(TagFilter.includeTags(tagExpressions)));
            // @formatter:on
            findRepeatableAnnotations(javaClass, Configuration.class).forEach(configuration ->
            		request.configurationParameter(configuration.key(), configuration.value()));
			return request.build();
		}

		private List<ClassSelector> selectClasses(Class<?>[] classes) {
			return Arrays.stream(classes).map(DiscoverySelectors::selectClass).collect(toList());
		}

		private List<PackageSelector> selectPackages(String[] packages) {
			return Arrays.stream(packages).map(DiscoverySelectors::selectPackage).collect(toList());
		}
	}

}
