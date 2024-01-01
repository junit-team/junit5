/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.runner;

import static java.util.stream.Collectors.toList;
import static org.apiguardian.api.API.Status.DEPRECATED;
import static org.junit.platform.commons.support.AnnotationSupport.isAnnotated;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.suite.commons.SuiteLauncherDiscoveryRequestBuilder.request;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apiguardian.api.API;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.core.LauncherFactory;
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
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;
import org.junit.platform.suite.commons.SuiteLauncherDiscoveryRequestBuilder;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Filterable;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.notification.RunNotifier;

/**
 * JUnit 4 based {@link Runner} which runs tests on the JUnit Platform in a
 * JUnit 4 environment.
 *
 * <p>Annotating a class with {@code @RunWith(JUnitPlatform.class)} allows it
 * to be run with IDEs and build systems that support JUnit 4 but do not yet
 * support the JUnit Platform directly.
 *
 * <p>Please note that test classes and suites annotated with
 * {@code @RunWith(JUnitPlatform.class)} <em>cannot</em> be executed directly on
 * the JUnit Platform (or as a "JUnit 5" test as documented in some IDEs). Such
 * classes and suites can only be executed using JUnit 4 infrastructure.
 *
 * <p>Consult the various annotations in the {@code org.junit.platform.suite.api}
 * package for configuration options.
 *
 * <p>If you do not use any configuration annotations from the
 * {@code org.junit.platform.suite.api} package, you can use this runner on a
 * test class whose programming model is supported on the JUnit Platform &mdash;
 * for example, a JUnit Jupiter test class. Note, however, that any test class
 * run with this runner must be {@code public} in order to be picked up by IDEs
 * and build tools.
 *
 * <p>When used on a class that serves as a test suite and the
 * {@link IncludeClassNamePatterns @IncludeClassNamePatterns} annotation is not
 * present, the default include pattern
 * {@value org.junit.platform.engine.discovery.ClassNameFilter#STANDARD_INCLUDE_PATTERN}
 * will be used in order to avoid loading classes unnecessarily (see {@link
 * org.junit.platform.engine.discovery.ClassNameFilter#STANDARD_INCLUDE_PATTERN
 * ClassNameFilter#STANDARD_INCLUDE_PATTERN}).
 *
 * @since 1.0
 * @see SelectClasses
 * @see SelectClasspathResource
 * @see SelectDirectories
 * @see SelectFile
 * @see SelectModules
 * @see SelectPackages
 * @see SelectUris
 * @see IncludeClassNamePatterns
 * @see ExcludeClassNamePatterns
 * @see IncludeEngines
 * @see ExcludeEngines
 * @see IncludePackages
 * @see ExcludePackages
 * @see IncludeTags
 * @see ExcludeTags
 * @see SuiteDisplayName
 * @see org.junit.platform.suite.api.UseTechnicalNames UseTechnicalNames
 * @see ConfigurationParameter
 * @deprecated since 1.8, in favor of the {@link Suite @Suite} support provided by
 * the {@code junit-platform-suite-engine} module; to be removed in JUnit Platform 2.0
 */
@API(status = DEPRECATED, since = "1.8")
@Deprecated
public class JUnitPlatform extends Runner implements Filterable {

	// @formatter:off
	private static final List<Class<? extends Annotation>> IMPLICIT_SUITE_ANNOTATIONS = Arrays.asList(
			SelectClasses.class,
			SelectClasspathResource.class,
			SelectDirectories.class,
			SelectFile.class,
			SelectFile.class,
			SelectModules.class,
			SelectPackages.class,
			SelectUris.class
	);
	// @formatter:on

	private final Class<?> testClass;
	private final Launcher launcher;

	private JUnitPlatformTestTree testTree;

	public JUnitPlatform(Class<?> testClass) {
		this(testClass, LauncherFactory.create());
	}

	// For testing only
	JUnitPlatform(Class<?> testClass, Launcher launcher) {
		this.launcher = launcher;
		this.testClass = testClass;
		this.testTree = generateTestTree(createDiscoveryRequest());
	}

	@Override
	public Description getDescription() {
		return this.testTree.getSuiteDescription();
	}

	@Override
	public void run(RunNotifier notifier) {
		this.launcher.execute(this.testTree.getTestPlan(), new JUnitPlatformRunnerListener(this.testTree, notifier));
	}

	private JUnitPlatformTestTree generateTestTree(LauncherDiscoveryRequest discoveryRequest) {
		TestPlan testPlan = this.launcher.discover(discoveryRequest);
		return new JUnitPlatformTestTree(testPlan, this.testClass);
	}

	private LauncherDiscoveryRequest createDiscoveryRequest() {
		SuiteLauncherDiscoveryRequestBuilder requestBuilder = request();
		// Allows @RunWith(JUnitPlatform.class) to be added to any test case
		boolean isSuite = isSuite();
		if (!isSuite) {
			requestBuilder.selectors(selectClass(this.testClass));
		}

		// @formatter:off
		return requestBuilder
				.filterStandardClassNamePatterns(isSuite)
				.suite(this.testClass)
				.build();
		// @formatter:on
	}

	private boolean isSuite() {
		// @formatter:off
		return IMPLICIT_SUITE_ANNOTATIONS.stream()
				.anyMatch(annotation -> isAnnotated(this.testClass, annotation));
		// @formatter:on
	}

	@Override
	public void filter(Filter filter) throws NoTestsRemainException {
		Set<TestIdentifier> filteredIdentifiers = this.testTree.getFilteredLeaves(filter);
		if (filteredIdentifiers.isEmpty()) {
			throw new NoTestsRemainException();
		}
		this.testTree = generateTestTree(createDiscoveryRequestForUniqueIds(filteredIdentifiers));
	}

	private LauncherDiscoveryRequest createDiscoveryRequestForUniqueIds(Set<TestIdentifier> testIdentifiers) {
		// @formatter:off
		List<DiscoverySelector> selectors = testIdentifiers.stream()
				.map(TestIdentifier::getUniqueIdObject)
				.map(DiscoverySelectors::selectUniqueId)
				.collect(toList());
		// @formatter:on
		return request().selectors(selectors).build();
	}

}
