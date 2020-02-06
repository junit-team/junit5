/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher.core;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.engine.UniqueId;

/**
 * Perform common validation checks on the result from the `discover()` method.
 *
 * @since 1.3
 */
class EngineDiscoveryResultValidator {

	/**
	 *  Perform common validation checks.
	 *
	 * @throws org.junit.platform.commons.PreconditionViolationException if any check fails
	 */
	void validate(TestEngine testEngine, TestDescriptor root) {
		Preconditions.notNull(root,
			() -> String.format(
				"The discover() method for TestEngine with ID '%s' must return a non-null root TestDescriptor.",
				testEngine.getId()));
		Preconditions.condition(isAcyclic(root),
			() -> String.format("The discover() method for TestEngine with ID '%s' returned a cyclic graph.",
				testEngine.getId()));
	}

	/**
	 * @return {@code true} if the tree does <em>not</em> contain a cycle; else {@code false}.
	 */
	boolean isAcyclic(TestDescriptor root) {
		Set<UniqueId> visited = new HashSet<>();
		visited.add(root.getUniqueId());
		Queue<TestDescriptor> queue = new ArrayDeque<>();
		queue.add(root);
		while (!queue.isEmpty()) {
			for (TestDescriptor child : queue.remove().getChildren()) {
				if (!visited.add(child.getUniqueId())) {
					return false; // id already known: cycle detected!
				}
				if (child.isContainer()) {
					queue.add(child);
				}
			}
		}
		return true;
	}

}
