/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher;

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;
import static org.apiguardian.api.API.Status.DEPRECATED;
import static org.apiguardian.api.API.Status.INTERNAL;
import static org.apiguardian.api.API.Status.STABLE;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import org.apiguardian.api.API;
import org.junit.platform.commons.PreconditionViolationException;
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
 * <p>This class is not intended to be extended by clients.
 *
 * @since 1.0
 * @see Launcher
 * @see TestExecutionListener
 */
@API(status = STABLE, since = "1.0")
public class TestPlan {

	private final Set<TestIdentifier> roots = Collections.synchronizedSet(new LinkedHashSet<>(4));

	private final Map<String, Set<TestIdentifier>> children = new ConcurrentHashMap<>(32);

	private final Map<String, TestIdentifier> allIdentifiers = new ConcurrentHashMap<>(32);

	private final boolean containsTests;

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
	@API(status = INTERNAL, since = "1.0")
	public static TestPlan from(Collection<TestDescriptor> engineDescriptors) {
		Preconditions.notNull(engineDescriptors, "Cannot create TestPlan from a null collection of TestDescriptors");
		TestPlan testPlan = new TestPlan(engineDescriptors.stream().anyMatch(TestDescriptor::containsTests));
		Visitor visitor = descriptor -> testPlan.add(TestIdentifier.from(descriptor));
		engineDescriptors.forEach(engineDescriptor -> engineDescriptor.accept(visitor));
		return testPlan;
	}

	@API(status = INTERNAL, since = "1.4")
	protected TestPlan(boolean containsTests) {
		this.containsTests = containsTests;
	}

	/**
	 * Add the supplied {@link TestIdentifier} to this test plan.
	 *
	 * @param testIdentifier the identifier to add; never {@code null}
	 * @deprecated Please discontinue use of this method. A future version of the
	 * JUnit Platform will ignore this call and eventually even throw an exception.
	 */
	@Deprecated
	@API(status = DEPRECATED, since = "1.4")
	public void add(TestIdentifier testIdentifier) {
		Preconditions.notNull(testIdentifier, "testIdentifier must not be null");
		allIdentifiers.put(testIdentifier.getUniqueId(), testIdentifier);
		if (testIdentifier.getParentId().isPresent()) {
			String parentId = testIdentifier.getParentId().get();
			Set<TestIdentifier> directChildren = children.computeIfAbsent(parentId,
				key -> Collections.synchronizedSet(new LinkedHashSet<>(16)));
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
		return child.getParentId().map(this::getTestIdentifier);
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
		Set<TestIdentifier> result = new LinkedHashSet<>(16);
		Set<TestIdentifier> children = getChildren(parent);
		result.addAll(children);
		for (TestIdentifier child : children) {
			result.addAll(getDescendants(child));
		}
		return unmodifiableSet(result);
	}

	/**
	 * Return whether this test plan contains any tests.
	 *
	 * <p>A test plan contains tests, if at least one of the contained engine
	 * descriptors {@linkplain TestDescriptor#containsTests(TestDescriptor)
	 * contains tests}.
	 *
	 * @return {@code true} if this test plan contains tests
	 * @see TestDescriptor#containsTests(TestDescriptor)
	 */
	public boolean containsTests() {
		return containsTests;
	}

}
