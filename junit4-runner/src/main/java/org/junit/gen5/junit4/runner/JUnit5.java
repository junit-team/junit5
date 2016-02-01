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
import static org.junit.gen5.launcher.main.TestDiscoveryRequestBuilder.request;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.commons.util.StringUtils;
import org.junit.gen5.engine.DiscoverySelector;
import org.junit.gen5.engine.discovery.ClassFilter;
import org.junit.gen5.engine.discovery.ClassSelector;
import org.junit.gen5.engine.discovery.PackageSelector;
import org.junit.gen5.engine.discovery.UniqueIdSelector;
import org.junit.gen5.launcher.EngineIdFilter;
import org.junit.gen5.launcher.Launcher;
import org.junit.gen5.launcher.PostDiscoveryFilter;
import org.junit.gen5.launcher.TagFilter;
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
 * JUnit 4 based {@link Runner} which runs tests that use the JUnit 5 programming and extension models.
 *
 * <p>Annotating a class with {@code @RunWith(JUnit5.class)} allows it to be run with IDEs and build systems that
 * support JUnit 4 but do not yet support the JUnit 5 APIs directly.
 *
 * <p>Consult the various annotations in this package for configuration options.
 *
 * <p>If you don't use any annotations, you can simply us this runner on a JUnit 5 test class.
 * Contrary to standard JUnit 5 test classes, the test class must be {@code public} in order
 * to be picked up by IDEs and build tools.
 *
 * @since 5.0
 * @see Classes
 * @see ClassNamePattern
 * @see Packages
 * @see UniqueIds
 * @see RequireTags
 * @see ExcludeTags
 * @see RequireEngine
 */
public class JUnit5 extends Runner implements Filterable {

	private static final Class<?>[] EMPTY_CLASS_ARRAY = new Class<?>[0];
	private static final String[] EMPTY_STRING_ARRAY = new String[0];
	private static final String EMPTY_STRING = "";

	private final Class<?> testClass;
	private final Launcher launcher;

	private TestDiscoveryRequest discoveryRequest;
	private JUnit5TestTree testTree;

	public JUnit5(Class<?> testClass) throws InitializationError {
		this(testClass, LauncherFactory.create());
	}

	// For testing only
	JUnit5(Class<?> testClass, Launcher launcher) throws InitializationError {
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
		JUnit5RunnerListener listener = new JUnit5RunnerListener(this.testTree, notifier);
		this.launcher.registerTestExecutionListeners(listener);
		this.launcher.execute(this.discoveryRequest);
	}

	private JUnit5TestTree generateTestTree() {
		Preconditions.notNull(this.discoveryRequest, "DiscoveryRequest must not be null");
		TestPlan plan = this.launcher.discover(this.discoveryRequest);
		return new JUnit5TestTree(plan, testClass);
	}

	private TestDiscoveryRequest createDiscoveryRequest() {
		List<DiscoverySelector> selectors = getSpecElementsFromAnnotations();

		// Allows to simply add @RunWith(JUnit5.class) to any JUnit5 test case
		if (selectors.isEmpty()) {
			selectors.add(ClassSelector.forClass(this.testClass));
		}

		TestDiscoveryRequest request = request().select(selectors).build();
		addFiltersFromAnnotations(request);
		return request;
	}

	private void addFiltersFromAnnotations(TestDiscoveryRequest request) {
		addClassNameMatchesFilter(request);
		addRequiredTagsFilter(request);
		addExcludedTagsFilter(request);
		addEngineIdFilter(request);
	}

	private List<DiscoverySelector> getSpecElementsFromAnnotations() {
		List<DiscoverySelector> selectors = new ArrayList<>();

		selectors.addAll(transform(getTestClasses(), ClassSelector::forClass));
		selectors.addAll(transform(getUniqueIds(), UniqueIdSelector::forUniqueId));
		selectors.addAll(transform(getPackageNames(), PackageSelector::forPackageName));

		return selectors;
	}

	private <T> List<DiscoverySelector> transform(T[] sourceElements, Function<T, DiscoverySelector> transformer) {
		return stream(sourceElements).map(transformer).collect(toList());
	}

	private void addClassNameMatchesFilter(TestDiscoveryRequest discoveryRequest) {
		String regex = getClassNameRegExPattern();
		if (!regex.isEmpty()) {
			discoveryRequest.addFilter(ClassFilter.byNamePattern(regex));
		}
	}

	private void addRequiredTagsFilter(TestDiscoveryRequest discoveryRequest) {
		String[] requiredTags = getRequiredTags();
		if (requiredTags.length > 0) {
			PostDiscoveryFilter tagNamesFilter = TagFilter.requireTags(requiredTags);
			discoveryRequest.addPostFilter(tagNamesFilter);
		}
	}

	private void addExcludedTagsFilter(TestDiscoveryRequest discoveryRequest) {
		String[] excludedTags = getExcludedTags();
		if (excludedTags.length > 0) {
			PostDiscoveryFilter excludeTagsFilter = TagFilter.excludeTags(excludedTags);
			discoveryRequest.addPostFilter(excludeTagsFilter);
		}
	}

	private void addEngineIdFilter(TestDiscoveryRequest discoveryRequest) {
		String engineId = getExplicitEngineId();
		if (StringUtils.isNotBlank(engineId)) {
			EngineIdFilter engineFilter = EngineIdFilter.byEngineId(engineId);
			discoveryRequest.addEngineIdFilter(engineFilter);
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

	private String[] getRequiredTags() {
		return getValueFromAnnotation(RequireTags.class, RequireTags::value, EMPTY_STRING_ARRAY);
	}

	private String[] getExcludedTags() {
		return getValueFromAnnotation(ExcludeTags.class, ExcludeTags::value, EMPTY_STRING_ARRAY);
	}

	private String getExplicitEngineId() {
		return getValueFromAnnotation(RequireEngine.class, RequireEngine::value, EMPTY_STRING).trim();
	}

	private String getClassNameRegExPattern() {
		return getValueFromAnnotation(ClassNamePattern.class, ClassNamePattern::value, EMPTY_STRING).trim();
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
				.map(Object::toString)
				.map(UniqueIdSelector::forUniqueId)
				.collect(toList());
		// @formatter:on
		return request().select(selectors).build();
	}
}
