/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.junit4.runner;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.junit.gen5.commons.meta.API.Usage.Maintained;
import static org.junit.gen5.launcher.EngineFilter.excludeEngines;
import static org.junit.gen5.launcher.EngineFilter.includeEngines;
import static org.junit.gen5.launcher.TagFilter.excludeTags;
import static org.junit.gen5.launcher.TagFilter.includeTags;
import static org.junit.gen5.launcher.main.TestDiscoveryRequestBuilder.request;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.junit.gen5.commons.meta.API;
import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.engine.DiscoverySelector;
import org.junit.gen5.engine.discovery.ClassFilter;
import org.junit.gen5.engine.discovery.ClassSelector;
import org.junit.gen5.engine.discovery.PackageSelector;
import org.junit.gen5.engine.discovery.UniqueIdSelector;
import org.junit.gen5.launcher.Launcher;
import org.junit.gen5.launcher.TestDiscoveryRequest;
import org.junit.gen5.launcher.TestIdentifier;
import org.junit.gen5.launcher.TestPlan;
import org.junit.gen5.launcher.main.LauncherFactory;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Filterable;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;

/**
 * JUnit 4 based {@link Runner} which runs tests on the JUnit Platform in a
 * JUnit 4 environment.
 *
 * <p>Annotating a class with {@code @RunWith(JUnitPlatform.class)} allows it
 * to be run with IDEs and build systems that support JUnit 4 but do not yet
 * support the JUnit Platform directly.
 *
 * <p>Consult the various annotations in this package for configuration options.
 *
 * <p>If you do not use any configuration annotations from this package, you
 * can simply use this runner on a test class whose programming model is
 * supported on the JUnit Platform &mdash; for example, a JUnit 5 test class.
 * Note, however, that any test class run with this runner must be {@code public}
 * in order to be picked up by IDEs and build tools.
 *
 * @since 5.0
 * @see Classes
 * @see FilterClassName
 * @see Packages
 * @see UniqueIds
 * @see IncludeTags
 * @see ExcludeTags
 * @see IncludeEngines
 * @see ExcludeEngines
 */
@API(Maintained)
public class JUnitPlatform extends Runner implements Filterable {

	private static final Class<?>[] EMPTY_CLASS_ARRAY = new Class<?>[0];
	private static final String[] EMPTY_STRING_ARRAY = new String[0];
	private static final String EMPTY_STRING = "";

	private final Class<?> testClass;
	private final Launcher launcher;

	private TestDiscoveryRequest discoveryRequest;
	private JUnitPlatformTestTree testTree;

	public JUnitPlatform(Class<?> testClass) throws InitializationError {
		this(testClass, LauncherFactory.create());
	}

	// For testing only
	JUnitPlatform(Class<?> testClass, Launcher launcher) throws InitializationError {
		this.launcher = launcher;
		this.testClass = testClass;
		this.discoveryRequest = createDiscoveryRequest();
		this.testTree = generateTestTree();
	}

	@Override
	public Description getDescription() {
		return this.testTree.getSuiteDescription();
	}

	@Override
	public void run(RunNotifier notifier) {
		JUnitPlatformRunnerListener listener = new JUnitPlatformRunnerListener(this.testTree, notifier);
		this.launcher.registerTestExecutionListeners(listener);
		this.launcher.execute(this.discoveryRequest);
	}

	private JUnitPlatformTestTree generateTestTree() {
		Preconditions.notNull(this.discoveryRequest, "DiscoveryRequest must not be null");
		TestPlan plan = this.launcher.discover(this.discoveryRequest);
		return new JUnitPlatformTestTree(plan, testClass);
	}

	private TestDiscoveryRequest createDiscoveryRequest() {
		List<DiscoverySelector> selectors = getSpecElementsFromAnnotations();

		// Allows to simply add @RunWith(JUnitPlatform.class) to any test case
		if (selectors.isEmpty()) {
			selectors.add(ClassSelector.selectClass(this.testClass));
		}

		TestDiscoveryRequest request = request().selectors(selectors).build();
		addFiltersFromAnnotations(request);
		return request;
	}

	private void addFiltersFromAnnotations(TestDiscoveryRequest request) {
		addClassNameMatchesFilter(request);

		addIncludedTagsFilter(request);
		addExcludedTagsFilter(request);

		addIncludedEnginesFilter(request);
		addExcludedEnginesFilter(request);
	}

	private List<DiscoverySelector> getSpecElementsFromAnnotations() {
		List<DiscoverySelector> selectors = new ArrayList<>();

		selectors.addAll(transform(getTestClasses(), ClassSelector::selectClass));
		selectors.addAll(transform(getUniqueIds(), UniqueIdSelector::selectUniqueId));
		selectors.addAll(transform(getPackageNames(), PackageSelector::selectPackage));

		return selectors;
	}

	private <T> List<DiscoverySelector> transform(T[] sourceElements, Function<T, DiscoverySelector> transformer) {
		return stream(sourceElements).map(transformer).collect(toList());
	}

	private void addClassNameMatchesFilter(TestDiscoveryRequest discoveryRequest) {
		String regex = getClassNameRegExPattern();
		if (!regex.isEmpty()) {
			discoveryRequest.addFilter(ClassFilter.byClassNamePattern(regex));
		}
	}

	private void addIncludedTagsFilter(TestDiscoveryRequest discoveryRequest) {
		String[] includedTags = getIncludedTags();
		if (includedTags.length > 0) {
			discoveryRequest.addPostFilter(includeTags(includedTags));
		}
	}

	private void addExcludedTagsFilter(TestDiscoveryRequest discoveryRequest) {
		String[] excludedTags = getExcludedTags();
		if (excludedTags.length > 0) {
			discoveryRequest.addPostFilter(excludeTags(excludedTags));
		}
	}

	private void addIncludedEnginesFilter(TestDiscoveryRequest discoveryRequest) {
		String[] engineIds = getIncludedEngineIds();
		if (engineIds.length > 0) {
			discoveryRequest.addEngineFilter(includeEngines(engineIds));
		}
	}

	private void addExcludedEnginesFilter(TestDiscoveryRequest discoveryRequest) {
		String[] engineIds = getExcludedEngineIds();
		if (engineIds.length > 0) {
			discoveryRequest.addEngineFilter(excludeEngines(engineIds));
		}
	}

	private Class<?>[] getTestClasses() {
		return getValueFromAnnotation(Classes.class, Classes::value, EMPTY_CLASS_ARRAY);
	}

	private String[] getUniqueIds() {
		return getValueFromAnnotation(UniqueIds.class, UniqueIds::value, EMPTY_STRING_ARRAY);
	}

	private String[] getPackageNames() {
		return getValueFromAnnotation(Packages.class, Packages::value, EMPTY_STRING_ARRAY);
	}

	private String[] getIncludedTags() {
		return getValueFromAnnotation(IncludeTags.class, IncludeTags::value, EMPTY_STRING_ARRAY);
	}

	private String[] getExcludedTags() {
		return getValueFromAnnotation(ExcludeTags.class, ExcludeTags::value, EMPTY_STRING_ARRAY);
	}

	private String[] getIncludedEngineIds() {
		return getValueFromAnnotation(IncludeEngines.class, IncludeEngines::value, EMPTY_STRING_ARRAY);
	}

	private String[] getExcludedEngineIds() {
		return getValueFromAnnotation(ExcludeEngines.class, ExcludeEngines::value, EMPTY_STRING_ARRAY);
	}

	private String getClassNameRegExPattern() {
		return getValueFromAnnotation(FilterClassName.class, FilterClassName::value, EMPTY_STRING).trim();
	}

	private <A extends Annotation, V> V getValueFromAnnotation(Class<A> annotationClass, Function<A, V> extractor,
			V defaultValue) {
		A annotation = this.testClass.getAnnotation(annotationClass);
		return (annotation != null ? extractor.apply(annotation) : defaultValue);
	}

	@Override
	public void filter(Filter filter) throws NoTestsRemainException {
		Set<TestIdentifier> filteredIdentifiers = testTree.getFilteredLeaves(filter);
		if (filteredIdentifiers.isEmpty()) {
			throw new NoTestsRemainException();
		}
		this.discoveryRequest = createDiscoveryRequestForUniqueIds(filteredIdentifiers);
		this.testTree = generateTestTree();
	}

	private TestDiscoveryRequest createDiscoveryRequestForUniqueIds(Set<TestIdentifier> testIdentifiers) {
		// @formatter:off
		List<DiscoverySelector> selectors = testIdentifiers.stream()
				.map(TestIdentifier::getUniqueId)
				.map(UniqueIdSelector::selectUniqueId)
				.collect(toList());
		// @formatter:on
		return request().selectors(selectors).build();
	}

}
