/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.launcher;

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;
import static org.junit.platform.commons.meta.API.Usage.Experimental;
import static org.junit.platform.commons.meta.API.Usage.Internal;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import org.junit.platform.commons.meta.API;
import org.junit.platform.commons.util.PreconditionViolationException;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestDescriptor.Visitor;

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
	 * Construct a new {@code TestPlan} from the supplied collection of
	 * {@link TestDescriptor TestDescriptors}.
	 *
	 * <p>Each supplied {@code TestDescriptor} is expected to be a descriptor
	 * for a {@link org.junit.platform.engine.TestEngine TestEngine}.
	 *
	 * @param engineDescriptors the engine test descriptors from which the test
	 * plan should be created; never {@code null}
	 * @return a new test plan
	 */
	@API(Internal)
	public static TestPlan from(Collection<TestDescriptor> engineDescriptors) {
		Preconditions.notNull(engineDescriptors, "Cannot create TestPlan from a null collection of TestDescriptors");
		TestPlan testPlan = new TestPlan();
		Visitor visitor = descriptor -> testPlan.add(TestIdentifier.from(descriptor));
		engineDescriptors.forEach(engineDescriptor -> engineDescriptor.accept(visitor));
		return testPlan;
	}

	private TestPlan() {
		/* no-op */
	}

	/**
	 * Add the supplied {@link TestIdentifier} to this test plan.
	 *
	 * @param testIdentifier the identifier to add; never {@code null}
	 */
	public void add(TestIdentifier testIdentifier) {
		Preconditions.notNull(testIdentifier, "testIdentifier must not be null");
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
	 * Get the root {@link TestIdentifier TestIdentifiers} for this test plan.
	 *
	 * @return an unmodifiable set of the root identifiers
	 */
	public Set<TestIdentifier> getRoots() {
		return unmodifiableSet(roots);
	}

	/**
	 * Get the parent of the supplied {@link TestIdentifier}.
	 *
	 * @param child the identifier to look up the parent for; never {@code null}
	 * @return an {@code Optional} containing the parent, if present
	 */
	public Optional<TestIdentifier> getParent(TestIdentifier child) {
		Preconditions.notNull(child, "child must not be null");
		Optional<String> optionalParentId = child.getParentId();
		if (optionalParentId.isPresent()) {
			return Optional.of(getTestIdentifier(optionalParentId.get()));
		}
		return Optional.empty();
	}

	/**
	 * Get the children of the supplied {@link TestIdentifier}.
	 *
	 * @param parent the identifier to look up the children for; never {@code null}
	 * @return an unmodifiable set of the parent's children, potentially empty
	 * @see #getChildren(String)
	 */
	public Set<TestIdentifier> getChildren(TestIdentifier parent) {
		Preconditions.notNull(parent, "parent must not be null");
		return getChildren(parent.getUniqueId());
	}

	/**
	 * Get the children of the supplied unique ID.
	 *
	 * @param parentId the unique ID to look up the children for; never
	 * {@code null} or blank
	 * @return an unmodifiable set of the parent's children, potentially empty
	 * @see #getChildren(TestIdentifier)
	 */
	public Set<TestIdentifier> getChildren(String parentId) {
		Preconditions.notBlank(parentId, "parent ID must not be null or blank");
		return children.containsKey(parentId) ? unmodifiableSet(children.get(parentId)) : emptySet();
	}

	/**
	 * Get the {@link TestIdentifier} with the supplied unique ID.
	 *
	 * @param uniqueId the unique ID to look up the identifier for; never
	 * {@code null} or blank
	 * @return the identifier with the supplied unique ID; never {@code null}
	 * @throws PreconditionViolationException if no {@code TestIdentifier}
	 * with the supplied unique ID is present in this test plan
	 */
	public TestIdentifier getTestIdentifier(String uniqueId) throws PreconditionViolationException {
		Preconditions.notBlank(uniqueId, "unique ID must not be null or blank");
		Preconditions.condition(allIdentifiers.containsKey(uniqueId),
			() -> "No TestIdentifier with unique ID [" + uniqueId + "] has been added to this TestPlan.");
		return allIdentifiers.get(uniqueId);
	}

	/**
	 * Count all {@link TestIdentifier TestIdentifiers} that satisfy the
	 * given {@linkplain Predicate predicate}.
	 *
	 * @param predicate a predicate which returns {@code true} for identifiers
	 * to be counted; never {@code null}
	 * @return the number of identifiers that satisfy the supplied predicate
	 */
	public long countTestIdentifiers(Predicate<? super TestIdentifier> predicate) {
		Preconditions.notNull(predicate, "Predicate must not be null");
		return allIdentifiers.values().stream().filter(predicate).count();
	}

	/**
	 * Get all descendants of the supplied {@link TestIdentifier} (i.e.,
	 * all of its children and their children, recursively).
	 *
	 * @param parent the identifier to look up the descendants for; never {@code null}
	 * @return an unmodifiable set of the parent's descendants, potentially empty
	 */
	public Set<TestIdentifier> getDescendants(TestIdentifier parent) {
		Preconditions.notNull(parent, "parent must not be null");
		Set<TestIdentifier> result = new LinkedHashSet<>();
		Set<TestIdentifier> children = getChildren(parent);
		result.addAll(children);
		for (TestIdentifier child : children) {
			result.addAll(getDescendants(child));
		}
		return unmodifiableSet(result);
	}

}
