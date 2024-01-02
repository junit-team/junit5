/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.console.tasks;

import static org.junit.platform.engine.discovery.ClassNameFilter.excludeClassNamePatterns;
import static org.junit.platform.engine.discovery.ClassNameFilter.includeClassNamePatterns;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClasspathRoots;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectModules;
import static org.junit.platform.engine.discovery.PackageNameFilter.excludePackageNames;
import static org.junit.platform.engine.discovery.PackageNameFilter.includePackageNames;
import static org.junit.platform.launcher.EngineFilter.excludeEngines;
import static org.junit.platform.launcher.EngineFilter.includeEngines;
import static org.junit.platform.launcher.TagFilter.excludeTags;
import static org.junit.platform.launcher.TagFilter.includeTags;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.platform.commons.util.ModuleUtils;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.console.options.TestDiscoveryOptions;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.discovery.ClassNameFilter;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.discovery.ClasspathRootSelector;
import org.junit.platform.engine.discovery.IterationSelector;
import org.junit.platform.engine.discovery.MethodSelector;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;

/**
 * @since 1.0
 */
class DiscoveryRequestCreator {

	LauncherDiscoveryRequest toDiscoveryRequest(TestDiscoveryOptions options) {
		LauncherDiscoveryRequestBuilder requestBuilder = request();
		List<? extends DiscoverySelector> selectors = createDiscoverySelectors(options);
		requestBuilder.selectors(selectors);
		addFilters(requestBuilder, options, selectors);
		requestBuilder.configurationParameters(options.getConfigurationParameters());
		return requestBuilder.build();
	}

	private List<? extends DiscoverySelector> createDiscoverySelectors(TestDiscoveryOptions options) {
		List<DiscoverySelector> explicitSelectors = options.getExplicitSelectors();
		if (options.isScanClasspath()) {
			Preconditions.condition(explicitSelectors.isEmpty(),
				"Scanning the classpath and using explicit selectors at the same time is not supported");
			return createClasspathRootSelectors(options);
		}
		if (options.isScanModulepath()) {
			Preconditions.condition(explicitSelectors.isEmpty(),
				"Scanning the module-path and using explicit selectors at the same time is not supported");
			return selectModules(ModuleUtils.findAllNonSystemBootModuleNames());
		}
		return Preconditions.notEmpty(explicitSelectors,
			"Please specify an explicit selector option or use --scan-class-path or --scan-modules");
	}

	private List<ClasspathRootSelector> createClasspathRootSelectors(TestDiscoveryOptions options) {
		Set<Path> classpathRoots = determineClasspathRoots(options);
		return selectClasspathRoots(classpathRoots);
	}

	private Set<Path> determineClasspathRoots(TestDiscoveryOptions options) {
		if (options.getSelectedClasspathEntries().isEmpty()) {
			Set<Path> rootDirs = new LinkedHashSet<>(ReflectionUtils.getAllClasspathRootDirectories());
			rootDirs.addAll(options.getExistingAdditionalClasspathEntries());
			return rootDirs;
		}
		return new LinkedHashSet<>(options.getSelectedClasspathEntries());
	}

	private void addFilters(LauncherDiscoveryRequestBuilder requestBuilder, TestDiscoveryOptions options,
			List<? extends DiscoverySelector> selectors) {
		requestBuilder.filters(includedClassNamePatterns(options, selectors));

		if (!options.getExcludedClassNamePatterns().isEmpty()) {
			requestBuilder.filters(
				excludeClassNamePatterns(options.getExcludedClassNamePatterns().toArray(new String[0])));
		}

		if (!options.getIncludedPackages().isEmpty()) {
			requestBuilder.filters(includePackageNames(options.getIncludedPackages()));
		}

		if (!options.getExcludedPackages().isEmpty()) {
			requestBuilder.filters(excludePackageNames(options.getExcludedPackages()));
		}

		if (!options.getIncludedTagExpressions().isEmpty()) {
			requestBuilder.filters(includeTags(options.getIncludedTagExpressions()));
		}

		if (!options.getExcludedTagExpressions().isEmpty()) {
			requestBuilder.filters(excludeTags(options.getExcludedTagExpressions()));
		}

		if (!options.getIncludedEngines().isEmpty()) {
			requestBuilder.filters(includeEngines(options.getIncludedEngines()));
		}

		if (!options.getExcludedEngines().isEmpty()) {
			requestBuilder.filters(excludeEngines(options.getExcludedEngines()));
		}
	}

	private ClassNameFilter includedClassNamePatterns(TestDiscoveryOptions options,
			List<? extends DiscoverySelector> selectors) {
		Stream<String> patternStreams = Stream.concat( //
			options.getIncludedClassNamePatterns().stream(), //
			selectors.stream() //
					.map(selector -> selector instanceof IterationSelector
							? ((IterationSelector) selector).getParentSelector()
							: selector) //
					.map(selector -> {
						if (selector instanceof ClassSelector) {
							return ((ClassSelector) selector).getClassName();
						}
						if (selector instanceof MethodSelector) {
							return ((MethodSelector) selector).getClassName();
						}
						return null;
					}) //
					.filter(Objects::nonNull) //
					.map(Pattern::quote));
		return includeClassNamePatterns(patternStreams.toArray(String[]::new));
	}

}
