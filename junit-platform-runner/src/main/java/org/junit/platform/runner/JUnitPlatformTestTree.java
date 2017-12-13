/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.runner;

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toSet;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import org.junit.platform.commons.util.AnnotationUtils;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.StringUtils;
import org.junit.platform.engine.FilterResult;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.launcher.PostDiscoveryFilter;
import org.junit.platform.launcher.TestCollection;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.suite.api.SuiteDisplayName;
import org.junit.platform.suite.api.UseTechnicalNames;
import org.junit.runner.Description;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.notification.RunNotifier;

/**
 * @since 1.0
 */
class JUnitPlatformTestTree {

	private final Map<TestIdentifier, Description> descriptions = new HashMap<>();
	private final TestCollection testCollection;
	private final Function<TestIdentifier, String> nameExtractor;
	private final String suiteDisplayName;
	private Description suiteDescription;

	JUnitPlatformTestTree(TestCollection testCollection, Class<?> testClass) {
		this.testCollection = testCollection;
		if (useTechnicalNames(testClass)) {
			this.nameExtractor = JUnitPlatformTestTree::getTechnicalName;
			this.suiteDisplayName = testClass.getName();
		}
		else {
			this.nameExtractor = TestIdentifier::getDisplayName;
			this.suiteDisplayName = getSuiteDisplayName(testClass);
		}
		this.suiteDescription = generateSuiteDescription(testCollection);
	}

	private static boolean useTechnicalNames(Class<?> testClass) {
		return testClass.isAnnotationPresent(UseTechnicalNames.class);
	}

	Description getSuiteDescription() {
		return this.suiteDescription;
	}

	Description getDescription(TestIdentifier identifier) {
		return this.descriptions.get(identifier);
	}

	void executeTests(RunNotifier notifier) {
		JUnitPlatformRunnerListener listener = new JUnitPlatformRunnerListener(this, notifier);
		this.testCollection.execute(listener);
	}

	void filter(Filter filter) throws NoTestsRemainException {
		Set<TestIdentifier> filteredIdentifiers = getFilteredLeaves(filter);
		if (filteredIdentifiers.isEmpty()) {
			throw new NoTestsRemainException();
		}

		PostDiscoveryFilter postDiscoveryFilter = (TestDescriptor t) -> {
			return FilterResult.includedIf(filteredIdentifiers.contains(TestIdentifier.from(t)));
		};
		testCollection.applyPostDiscoveryFilters(postDiscoveryFilter);

		// Update our fields to match the testCollection
		descriptions.clear();
		suiteDescription = generateSuiteDescription(testCollection);
	}

	private Description generateSuiteDescription(TestCollection testCollection) {
		Description suiteDescription = Description.createSuiteDescription(suiteDisplayName);
		buildDescriptionTree(suiteDescription, testCollection);
		return suiteDescription;
	}

	private String getSuiteDisplayName(Class<?> testClass) {
		// @formatter:off
		return AnnotationUtils.findAnnotation(testClass, SuiteDisplayName.class)
				.map(SuiteDisplayName::value)
				.filter(StringUtils::isNotBlank)
				.orElse(testClass.getName());
		// @formatter:on
	}

	private void buildDescriptionTree(Description suiteDescription, TestCollection testCollection) {
		TestPlan testPlan = testCollection.testPlan();
		testPlan.getRoots().stream().filter(testIdentifier -> !testPlan.getChildren(testIdentifier).isEmpty()).forEach(
			testIdentifier -> buildDescription(testIdentifier, suiteDescription, testPlan));
	}

	void addDynamicDescription(TestIdentifier newIdentifier, String parentId) {
		TestPlan plan = this.testCollection.testPlan();
		TestIdentifier parentTestIdentifier = plan.getTestIdentifier(parentId);
		Description parent = getDescription(parentTestIdentifier);

		if (parent == null) {
			boolean parentIsRoot = plan.getRoots().contains(parentTestIdentifier);
			Preconditions.condition(parentIsRoot,
				() -> String.format("No matching Description for parentId=%s (testIdentifer=%s)", parentId,
					parentTestIdentifier));
			// This root was filtered out in buildDescriptionTree() because it had no children; add it back
			parent = buildDescription(parentTestIdentifier, suiteDescription, plan);
		}

		plan.add(newIdentifier);
		buildDescription(newIdentifier, parent, plan);
	}

	private Description buildDescription(TestIdentifier identifier, Description parent, TestPlan testPlan) {
		Description newDescription = createJUnit4Description(identifier, testPlan);
		parent.addChild(newDescription);
		this.descriptions.put(identifier, newDescription);
		testPlan.getChildren(identifier).forEach(
			testIdentifier -> buildDescription(testIdentifier, newDescription, testPlan));
		return newDescription;
	}

	private Description createJUnit4Description(TestIdentifier identifier, TestPlan testPlan) {
		String name = nameExtractor.apply(identifier);
		if (identifier.isTest()) {
			String containerName = testPlan.getParent(identifier).map(nameExtractor).orElse("<unrooted>");
			return Description.createTestDescription(containerName, name, identifier.getUniqueId());
		}
		return Description.createSuiteDescription(name, identifier.getUniqueId());
	}

	private static String getTechnicalName(TestIdentifier testIdentifier) {
		Optional<TestSource> optionalSource = testIdentifier.getSource();
		if (optionalSource.isPresent()) {
			TestSource source = optionalSource.get();
			if (source instanceof ClassSource) {
				return ((ClassSource) source).getJavaClass().getName();
			}
			else if (source instanceof MethodSource) {
				MethodSource methodSource = (MethodSource) source;
				String methodParameterTypes = methodSource.getMethodParameterTypes();
				if (StringUtils.isBlank(methodParameterTypes)) {
					return methodSource.getMethodName();
				}
				return String.format("%s(%s)", methodSource.getMethodName(), methodParameterTypes);
			}
		}

		// Else fall back to display name
		return testIdentifier.getDisplayName();
	}

	Set<TestIdentifier> getTestsInSubtree(TestIdentifier ancestor) {
		// @formatter:off
		return testCollection.testPlan().getDescendants(ancestor).stream()
				.filter(TestIdentifier::isTest)
				.collect(toCollection(LinkedHashSet::new));
		// @formatter:on
	}

	private Set<TestIdentifier> getFilteredLeaves(Filter filter) {
		Set<TestIdentifier> identifiers = applyFilterToDescriptions(filter);
		return removeNonLeafIdentifiers(identifiers);
	}

	private Set<TestIdentifier> removeNonLeafIdentifiers(Set<TestIdentifier> identifiers) {
		return identifiers.stream().filter(isALeaf(identifiers)).collect(toSet());
	}

	private Predicate<? super TestIdentifier> isALeaf(Set<TestIdentifier> identifiers) {
		return testIdentifier -> {
			TestPlan plan = testCollection.testPlan();
			Set<TestIdentifier> descendants = plan.getDescendants(testIdentifier);
			return identifiers.stream().noneMatch(descendants::contains);
		};
	}

	private Set<TestIdentifier> applyFilterToDescriptions(Filter filter) {
		// @formatter:off
		return descriptions.entrySet()
				.stream()
				.filter(entry -> filter.shouldRun(entry.getValue()))
				.map(Entry::getKey)
				.collect(toSet());
		// @formatter:on
	}

}
