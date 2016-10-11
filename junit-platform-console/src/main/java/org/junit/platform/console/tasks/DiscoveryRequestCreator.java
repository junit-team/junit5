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

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static org.junit.platform.engine.discovery.ClassNameFilter.includeClassNamePatterns;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClasspathRoots;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectJavaClass;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectJavaMethod;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectJavaPackage;
import static org.junit.platform.launcher.EngineFilter.excludeEngines;
import static org.junit.platform.launcher.EngineFilter.includeEngines;
import static org.junit.platform.launcher.TagFilter.excludeTags;
import static org.junit.platform.launcher.TagFilter.includeTags;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.platform.commons.util.PreconditionViolationException;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.console.options.CommandLineOptions;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.discovery.JavaClassSelector;
import org.junit.platform.engine.discovery.JavaMethodSelector;
import org.junit.platform.engine.discovery.JavaPackageSelector;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;

/**
 * @since 1.0
 */
class DiscoveryRequestCreator {

	LauncherDiscoveryRequest toDiscoveryRequest(CommandLineOptions options) {
		LauncherDiscoveryRequestBuilder requestBuilder = createRequestBuilder(options);
		addFilters(requestBuilder, options);
		return requestBuilder.build();
	}

	private LauncherDiscoveryRequestBuilder createRequestBuilder(CommandLineOptions options) {
		if (options.isScanClasspath()) {
			return createBuilderForClasspathScanning(options);
		}
		return createNameBasedBuilder(options);
	}

	private LauncherDiscoveryRequestBuilder createBuilderForClasspathScanning(CommandLineOptions options) {
		Set<Path> rootDirectoriesToScan = determineClasspathRootDirectories(options);
		return request().selectors(selectClasspathRoots(rootDirectoriesToScan));
	}

	private Set<Path> determineClasspathRootDirectories(CommandLineOptions options) {
		if (options.getArguments().isEmpty()) {
			Set<Path> rootDirs = new LinkedHashSet<>(ReflectionUtils.getAllClasspathRootDirectories());
			rootDirs.addAll(options.getAdditionalClasspathEntries());
			return rootDirs;
		}
		return options.getArguments().stream().map(Paths::get).collect(toCollection(LinkedHashSet::new));
	}

	private LauncherDiscoveryRequestBuilder createNameBasedBuilder(CommandLineOptions options) {
		Preconditions.notEmpty(options.getArguments(), "No arguments were supplied to the ConsoleLauncher");
		return request().selectors(selectNames(options.getArguments()));
	}

	private void addFilters(LauncherDiscoveryRequestBuilder requestBuilder, CommandLineOptions options) {
		requestBuilder.filters(includeClassNamePatterns(options.getIncludeClassNamePattern()));

		if (!options.getIncludedTags().isEmpty()) {
			requestBuilder.filters(includeTags(options.getIncludedTags()));
		}

		if (!options.getExcludedTags().isEmpty()) {
			requestBuilder.filters(excludeTags(options.getExcludedTags()));
		}

		if (!options.getIncludedEngines().isEmpty()) {
			requestBuilder.filters(includeEngines(options.getIncludedEngines()));
		}

		if (!options.getExcludedEngines().isEmpty()) {
			requestBuilder.filters(excludeEngines(options.getExcludedEngines()));
		}
	}

	/**
	 * Create a list of {@link DiscoverySelector DiscoverySelectors} for the
	 * supplied names.
	 *
	 * <p>Consult the documentation for {@link #selectName(String)} for details
	 * on what types of names are supported.
	 *
	 * @param names the names to select; never {@code null}
	 * @return a list of {@code DiscoverySelectors} for the supplied names;
	 * potentially empty
	 */
	private static List<DiscoverySelector> selectNames(Collection<String> names) {
		Preconditions.notNull(names, "names collection must not be null");
		return names.stream().map(DiscoveryRequestCreator::selectName).collect(toList());
	}

	/**
	 * Create a {@link DiscoverySelector} for the supplied name.
	 *
	 * <h3>Supported Name Types</h3>
	 * <ul>
	 * <li>package: fully qualified package name</li>
	 * <li>class: fully qualified class name</li>
	 * <li>method: fully qualified method name</li>
	 * </ul>
	 *
	 * @param name the name to select; never {@code null} or blank
	 * @return an instance of {@link JavaClassSelector}, {@link JavaMethodSelector}, or
	 * {@link JavaPackageSelector}
	 * @throws PreconditionViolationException if the supplied name is {@code null},
	 * blank, or does not specify a class, method, or package
	 */
	private static DiscoverySelector selectName(String name) throws PreconditionViolationException {
		Preconditions.notBlank(name, "name must not be null or blank");

		try {
			Optional<Class<?>> classOptional = ReflectionUtils.loadClass(name);
			if (classOptional.isPresent()) {
				return selectJavaClass(classOptional.get());
			}
		}
		catch (Exception ex) {
			// ignore
		}

		try {
			Optional<Method> methodOptional = ReflectionUtils.loadMethod(name);
			if (methodOptional.isPresent()) {
				Method method = methodOptional.get();
				return selectJavaMethod(method.getDeclaringClass(), method);
			}
		}
		catch (Exception ex) {
			// ignore
		}

		if (ReflectionUtils.isPackage(name)) {
			return selectJavaPackage(name);
		}

		throw new PreconditionViolationException(
			String.format("'%s' specifies neither a class, a method, nor a package.", name));
	}

}
