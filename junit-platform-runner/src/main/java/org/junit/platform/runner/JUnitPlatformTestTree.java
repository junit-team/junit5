/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
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

import org.junit.platform.commons.util.StringUtils;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.suite.api.UseTechnicalNames;
import org.junit.runner.Description;
import org.junit.runner.manipulation.Filter;

/**
 * @since 1.0
 */
class JUnitPlatformTestTree {

	private final Map<TestIdentifier, Description> descriptions = new HashMap<>();
	private final TestPlan plan;
	private final Function<TestIdentifier, String> nameExtractor;
	private final Description suiteDescription;

	JUnitPlatformTestTree(TestPlan plan, Class<?> testClass) {
		this.plan = plan;
		this.nameExtractor = useTechnicalNames(testClass) ? this::getTechnicalName : TestIdentifier::getDisplayName;
		this.suiteDescription = generateSuiteDescription(plan, testClass);
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

	private Description generateSuiteDescription(TestPlan testPlan, Class<?> testClass) {
		Description suiteDescription = Description.createSuiteDescription(testClass.getName());
		buildDescriptionTree(suiteDescription, testPlan);
		return suiteDescription;
	}

	private void buildDescriptionTree(Description suiteDescription, TestPlan testPlan) {
		testPlan.getRoots().forEach(testIdentifier -> buildDescription(testIdentifier, suiteDescription, testPlan));
	}

	void addDynamicDescription(TestIdentifier newIdentifier, String parentId) {
		Description parent = getDescription(this.plan.getTestIdentifier(parentId));
		this.plan.add(newIdentifier);
		buildDescription(newIdentifier, parent, this.plan);
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
			return Description.createTestDescription(containerName, name, identifier.getUniqueId());
		}
		return Description.createSuiteDescription(name, identifier.getUniqueId());
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
				else {
					return String.format("%s(%s)", methodSource.getMethodName(), methodParameterTypes);
				}
			}
		}

		// Else fall back to display name
		return testIdentifier.getDisplayName();
	}

	Set<TestIdentifier> getTestsInSubtree(TestIdentifier ancestor) {
		// @formatter:off
		return plan.getDescendants(ancestor).stream()
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
