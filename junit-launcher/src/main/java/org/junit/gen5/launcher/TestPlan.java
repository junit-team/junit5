/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.launcher;

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;

import java.util.*;
import java.util.function.Predicate;

import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.launcher.main.Launcher;

/**
 * Describes the tree of tests and containers as discovered by a
 * {@link Launcher}.
 *
 * <p>Tests and containers are represented by {@link TestIdentifier} instances.
 * The complete set of identifiers comprises a tree-like structure. However,
 * each identifier only stores the unique ID of its parent. This class provides
 * a number of helpful methods to retrieve the
 * {@linkplain #getParent(TestIdentifier) parent},
 * {@linkplain #getChildren(TestIdentifier) children}, and
 * {@linkplain #getDescendants(TestIdentifier) descendants} of an identifier.
 *
 * <p>While the contained instances of {@link TestIdentifier} are immutable,
 * instances of this class contain mutable state. E.g. when a dynamic test is
 * registered at runtime, it is added to the original test plan as reported to
 * {@link TestExecutionListener} implementations.
 *
 * @since 5.0
 * @see Launcher
 * @see TestExecutionListener
 */
public final class TestPlan {
	private final Set<TestIdentifier> roots = new LinkedHashSet<>();
	private final Map<TestId, LinkedHashSet<TestIdentifier>> children = new LinkedHashMap<>();
	private final Map<TestId, TestIdentifier> allIdentifiers = new LinkedHashMap<>();

	public static TestPlan from(Collection<TestDescriptor> engineDescriptors) {
		TestPlan testPlan = new TestPlan();
		// @formatter:off
		engineDescriptors.stream().forEach(testEngine -> testEngine.accept(
				(descriptor, remove) -> testPlan.add(TestIdentifier.from(descriptor))));
		// @formatter:on
		return testPlan;
	}

	public void add(TestIdentifier testIdentifier) {
		allIdentifiers.put(testIdentifier.getUniqueId(), testIdentifier);
		if (testIdentifier.getParentId().isPresent()) {
			TestId parentId = testIdentifier.getParentId().get();
			Set<TestIdentifier> directChildren = children.computeIfAbsent(parentId, key -> new LinkedHashSet<>());
			directChildren.add(testIdentifier);
		}
		else {
			roots.add(testIdentifier);
		}
	}

	/**
	 * Returns the roots of this test plan.
	 *
	 * @return the unmodifiable set of root identifiers
	 */
	public Set<TestIdentifier> getRoots() {
		return unmodifiableSet(roots);
	}

	/**
	 * Returns the parent of an identifier, if present.
	 *
	 * @param child the identifier to look up the parent for
	 * @return an {@link Optional} containing the parent, if present; otherwise empty.
	 */
	public Optional<TestIdentifier> getParent(TestIdentifier child) {
		Optional<TestId> optionalParentId = child.getParentId();
		if (optionalParentId.isPresent()) {
			return Optional.of(getTestIdentifier(optionalParentId.get()));
		}
		return Optional.empty();
	}

	/**
	 * Returns the children of an identifier, possibly an empty set.
	 *
	 * @param parent the identifier to look up the children for
	 * @return the unmodifiable set of the {@code parent}'s children, if any;
	 * otherwise empty.
	 */
	public Set<TestIdentifier> getChildren(TestIdentifier parent) {
		return getChildren(parent.getUniqueId());
	}

	/**
	 * Returns the children of a parent {@link TestId}.
	 *
	 * @param parentId the parent ID to look up the children for
	 * @return the unmodifiable set of the {@code parentId}'s children, if any;
	 * otherwise empty.
	 */
	public Set<TestIdentifier> getChildren(TestId parentId) {
		return children.containsKey(parentId) ? unmodifiableSet(children.get(parentId)) : emptySet();
	}

	/**
	 * Returns the {@link TestIdentifier} for a {@link TestId}.
	 *
	 * @param testId the unique ID to look up the identifier for
	 * @return the identifier with the specified unique ID
	 * @throws IllegalArgumentException if no {@link TestIdentifier} with the
	 * specified unique ID has been added to this test plan
	 */
	public TestIdentifier getTestIdentifier(TestId testId) {
		Preconditions.condition(allIdentifiers.containsKey(testId),
			() -> "No TestIdentifier with this TestId has been added to this TestPlan: " + testId);
		return allIdentifiers.get(testId);
	}

	/**
	 * Counts all {@linkplain TestIdentifier identifiers} that satisfy a given
	 * {@linkplain Predicate predicate}.
	 *
	 * @param predicate a predicate which returns {@code true} for identifiers
	 * to be counted
	 * @return the number of identifiers that satisfy the specified
	 * {@code predicate}.
	 */
	public long countTestIdentifiers(Predicate<? super TestIdentifier> predicate) {
		return allIdentifiers.values().stream().filter(predicate).count();
	}

	/**
	 * Returns all descendants of an identifier, i.e all of its children and
	 * their children, recursively.
	 *
	 * @param parent the identifier to look up the descendants for
	 * @return the unmodifiable set of the {@code parent}'s descendants, if
	 * any; otherwise empty.
	 */
	public Set<TestIdentifier> getDescendants(TestIdentifier parent) {
		Set<TestIdentifier> result = new LinkedHashSet<>();
		Set<TestIdentifier> children = getChildren(parent);
		result.addAll(children);
		for (TestIdentifier child : children) {
			result.addAll(getDescendants(child));
		}
		return result;
	}
}
