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
import org.junit.platform.commons.util.StringUtils;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.suite.api.SuiteDisplayName;
import org.junit.runner.Description;
import org.junit.runner.manipulation.Filter;

/**
 * @since 1.0
 */
class JUnitPlatformTestTree {

	private final Map<TestIdentifier, Description> descriptions = new HashMap<>();
	private final TestPlan testPlan;
	private final Function<TestIdentifier, String> nameExtractor;
	private final Description suiteDescription;

	JUnitPlatformTestTree(TestPlan testPlan, Class<?> testClass) {
		this.testPlan = testPlan;
		this.nameExtractor = useTechnicalNames(testClass) ? this::getTechnicalName : TestIdentifier::getDisplayName;
		this.suiteDescription = generateSuiteDescription(testPlan, testClass);
	}

	public TestPlan getTestPlan() {
		return testPlan;
	}

	@SuppressWarnings("deprecation")
	private static boolean useTechnicalNames(Class<?> testClass) {
		return testClass.isAnnotationPresent(org.junit.platform.suite.api.UseTechnicalNames.class);
	}

	Description getSuiteDescription() {
		return this.suiteDescription;
	}

	Description getDescription(TestIdentifier identifier) {
		return this.descriptions.get(identifier);
	}

	private Description generateSuiteDescription(TestPlan testPlan, Class<?> testClass) {
		String displayName = useTechnicalNames(testClass) ? testClass.getName() : getSuiteDisplayName(testClass);
		Description suiteDescription = Description.createSuiteDescription(displayName);
		buildDescriptionTree(suiteDescription, testPlan);
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

	private void buildDescriptionTree(Description suiteDescription, TestPlan testPlan) {
		testPlan.getRoots().forEach(testIdentifier -> buildDescription(testIdentifier, suiteDescription, testPlan));
	}

	void addDynamicDescription(TestIdentifier newIdentifier, UniqueId parentId) {
		Description parent = getDescription(this.testPlan.getTestIdentifier(parentId));
		buildDescription(newIdentifier, parent, this.testPlan);
	}

	private void buildDescription(TestIdentifier identifier, Description parent, TestPlan testPlan) {
		Description newDescription = createJUnit4Description(identifier, testPlan);
		parent.addChild(newDescription);
		this.descriptions.put(identifier, newDescription);
		testPlan.getChildren(identifier).forEach(
			testIdentifier -> buildDescription(testIdentifier, newDescription, testPlan));
	}

	private Description createJUnit4Description(TestIdentifier identifier, TestPlan testPlan) {
		String name = nameExtractor.apply(identifier);
		if (identifier.isTest()) {
			String containerName = testPlan.getParent(identifier).map(nameExtractor).orElse("<unrooted>");
			return Description.createTestDescription(containerName, name, identifier.getUniqueIdObject());
		}
		return Description.createSuiteDescription(name, identifier.getUniqueIdObject());
	}

	private String getTechnicalName(TestIdentifier testIdentifier) {
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
		return testPlan.getDescendants(ancestor).stream()
				.filter(TestIdentifier::isTest)
				.collect(toCollection(LinkedHashSet::new));
		// @formatter:on
	}

	Set<TestIdentifier> getFilteredLeaves(Filter filter) {
		Set<TestIdentifier> identifiers = applyFilterToDescriptions(filter);
		return removeNonLeafIdentifiers(identifiers);
	}

	private Set<TestIdentifier> removeNonLeafIdentifiers(Set<TestIdentifier> identifiers) {
		return identifiers.stream().filter(isALeaf(identifiers)).collect(toSet());
	}

	private Predicate<? super TestIdentifier> isALeaf(Set<TestIdentifier> identifiers) {
		return testIdentifier -> {
			Set<TestIdentifier> descendants = testPlan.getDescendants(testIdentifier);
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
