/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.launcher;

import static java.util.Collections.*;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestDescriptor.Visitor;

/**
 * @since 5.0
 */
public final class TestPlan {

	private final Set<TestIdentifier> roots = new LinkedHashSet<>();
	private final Map<TestId, LinkedHashSet<TestIdentifier>> children = new LinkedHashMap<>();
	private final Map<TestId, TestIdentifier> allIdentifiers = new LinkedHashMap<>();

	static TestPlan from(TestDescriptor root) {
		TestPlan testPlan = new TestPlan();
		root.accept(new Visitor() {

			@Override
			public void visit(TestDescriptor descriptor, Runnable remove) {
				testPlan.add(TestIdentifier.from(descriptor));
			}
		});
		return testPlan;
	}

	void add(TestIdentifier testIdentifier) {
		allIdentifiers.put(testIdentifier.getUniqueId(), testIdentifier);
		if (testIdentifier.getParentId().isPresent()) {
			TestId parentId = testIdentifier.getParentId().get();
			getOrCreateChildrenSet(parentId).add(testIdentifier);
		}
		else {
			roots.add(testIdentifier);
		}
	}

	private LinkedHashSet<TestIdentifier> getOrCreateChildrenSet(TestId parentId) {
		if (!children.containsKey(parentId)) {
			children.put(parentId, new LinkedHashSet<>());
		}
		return children.get(parentId);
	}

	public Set<TestIdentifier> getRoots() {
		return unmodifiableSet(roots);
	}

	public Optional<TestIdentifier> getParent(TestIdentifier child) {
		Optional<TestId> optionalParentId = child.getParentId();
		if (optionalParentId.isPresent()) {
			return Optional.of(getTestIdentifier(optionalParentId.get()));
		}
		return Optional.empty();
	}

	public Set<TestIdentifier> getChildren(TestIdentifier parent) {
		return getChildren(parent.getUniqueId());
	}

	public Set<TestIdentifier> getChildren(TestId parentId) {
		return children.containsKey(parentId) ? unmodifiableSet(children.get(parentId)) : emptySet();
	}

	public TestIdentifier getTestIdentifier(TestId testId) {
		Preconditions.condition(allIdentifiers.containsKey(testId),
			() -> "No TestIdentifier with this TestId has been added to this TestPlan: " + testId);
		return allIdentifiers.get(testId);
	}

	public long countTestIdentifiers(Predicate<? super TestIdentifier> predicate) {
		return allIdentifiers.values().stream().filter(predicate).count();
	}

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
