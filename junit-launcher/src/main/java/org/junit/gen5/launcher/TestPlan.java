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
import static org.junit.gen5.commons.meta.API.Usage.Experimental;
import static org.junit.gen5.commons.meta.API.Usage.Internal;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import org.junit.gen5.commons.meta.API;
import org.junit.gen5.commons.util.PreconditionViolationException;
import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestDescriptor.Visitor;

/**
 * {@code TestPlan} describes the tree of tests and containers as discovered
 * by a {@link Launcher}.
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
 * instances of this class contain mutable state. For example, when a dynamic
 * test is registered at runtime, it is added to the original test plan and
 * reported to {@link TestExecutionListener} implementations.
 *
 * @since 5.0
 * @see Launcher
 * @see TestExecutionListener
 */
@API(Experimental)
public final class TestPlan {

	private final Set<TestIdentifier> roots = new LinkedHashSet<>();
	private final Map<String, LinkedHashSet<TestIdentifier>> children = new LinkedHashMap<>();
	private final Map<String, TestIdentifier> allIdentifiers = new LinkedHashMap<>();

	/**
	 * Constructs a new {@code TestPlan} from a collection of
	 * {@linkplain TestDescriptor test descriptors}.
	 */
	@API(Internal)
	public static TestPlan from(Collection<TestDescriptor> engineDescriptors) {
		TestPlan testPlan = new TestPlan();
		Visitor visitor = descriptor -> testPlan.add(TestIdentifier.from(descriptor));
		engineDescriptors.forEach(engineDescriptor -> engineDescriptor.accept(visitor));
		return testPlan;
	}

	private TestPlan() {
	}

	/**
	 * Add the supplied {@link TestIdentifier} to this test plan.
	 *
	 * @param testIdentifier the identifier to add
	 */
	public void add(TestIdentifier testIdentifier) {
		allIdentifiers.put(testIdentifier.getUniqueId(), testIdentifier);
		if (testIdentifier.getParentId().isPresent()) {
			String parentId = testIdentifier.getParentId().get();
			Set<TestIdentifier> directChildren = children.computeIfAbsent(parentId, key -> new LinkedHashSet<>());
			directChildren.add(testIdentifier);
		}
		else {
			roots.add(testIdentifier);
		}
	}

	/**
	 * Get the root identifiers of this test plan.
	 *
	 * @return an unmodifiable set of the root identifiers
	 */
	public Set<TestIdentifier> getRoots() {
		return unmodifiableSet(roots);
	}

	/**
	 * Get the parent of supplied {@link TestIdentifier}.
	 *
	 * @param child the identifier to look up the parent for
	 * @return an {@code Optional} containing the parent, if present
	 */
	public Optional<TestIdentifier> getParent(TestIdentifier child) {
		Optional<String> optionalParentId = child.getParentId();
		if (optionalParentId.isPresent()) {
			return Optional.of(getTestIdentifier(optionalParentId.get()));
		}
		return Optional.empty();
	}

	/**
	 * Get the children of supplied {@link TestIdentifier}.
	 *
	 * @param parent the identifier to look up the children for
	 * @return an unmodifiable set of the parent's children, potentially empty
	 * @see #getChildren(String)
	 */
	public Set<TestIdentifier> getChildren(TestIdentifier parent) {
		return getChildren(parent.getUniqueId());
	}

	/**
	 * Get the children of the supplied unique ID.
	 *
	 * @param parentId the ID to look up the children for
	 * @return an unmodifiable set of the parent's children, potentially empty
	 * @see #getChildren(TestIdentifier)
	 */
	public Set<TestIdentifier> getChildren(String parentId) {
		return children.containsKey(parentId) ? unmodifiableSet(children.get(parentId)) : emptySet();
	}

	/**
	 * Get the {@link TestIdentifier} with the supplied unique ID.
	 *
	 * @param uniqueId the unique ID to look up the identifier for
	 * @return the identifier with the supplied unique ID
	 * @throws PreconditionViolationException if no {@code TestIdentifier}
	 * with the supplied unique ID is present in this test plan
	 */
	public TestIdentifier getTestIdentifier(String uniqueId) throws PreconditionViolationException {
		Preconditions.condition(allIdentifiers.containsKey(uniqueId),
			() -> "No TestIdentifier with this unique ID has been added to this TestPlan: " + uniqueId);
		return allIdentifiers.get(uniqueId);
	}

	/**
	 * Count all {@linkplain TestIdentifier identifiers} that satisfy the
	 * given {@linkplain Predicate predicate}.
	 *
	 * @param predicate a predicate which returns {@code true} for identifiers
	 * to be counted
	 * @return the number of identifiers that satisfy the supplied predicate
	 */
	public long countTestIdentifiers(Predicate<? super TestIdentifier> predicate) {
		return allIdentifiers.values().stream().filter(predicate).count();
	}

	/**
	 * Get all descendants of the supplied {@link TestIdentifier} (i.e.,
	 * all of its children and their children, recursively).
	 *
	 * @param parent the identifier to look up the descendants for
	 * @return an unmodifiable set of the parent's descendants, potentially empty
	 */
	public Set<TestIdentifier> getDescendants(TestIdentifier parent) {
		Set<TestIdentifier> result = new LinkedHashSet<>();
		Set<TestIdentifier> children = getChildren(parent);
		result.addAll(children);
		for (TestIdentifier child : children) {
			result.addAll(getDescendants(child));
		}
		return unmodifiableSet(result);
	}

}
