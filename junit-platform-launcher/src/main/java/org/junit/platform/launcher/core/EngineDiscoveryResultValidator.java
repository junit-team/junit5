/*
 * Copyright 2015-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher.core;

import java.util.*;

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
		Optional<String> cyclicGraphInfo = getCyclicGraphInfo(root);
		Preconditions.condition(!cyclicGraphInfo.isPresent(), () -> String.format(
			"The discover() method for TestEngine with ID '%s' returned a cyclic graph: " + cyclicGraphInfo));
	}

	/**
	 * @return {@code true} if the tree does <em>not</em> contain a cycle; else {@code false}.
	 */
	boolean isAcyclic(TestDescriptor root) {
		return !getCyclicGraphInfo(root).isPresent();
	}

	Optional<String> getCyclicGraphInfo(TestDescriptor root) {
		HashMap<UniqueId, Optional<UniqueId>> visited = new HashMap<>();

		visited.put(root.getUniqueId(), Optional.empty());
		Queue<TestDescriptor> queue = new ArrayDeque<>();
		queue.add(root);
		while (!queue.isEmpty()) {
			TestDescriptor parent = queue.remove();
			for (TestDescriptor child : parent.getChildren()) {
				UniqueId uid = child.getUniqueId();
				if (visited.containsKey(uid)) {// id already known: cycle detected!

					StringBuilder path1 = new StringBuilder();
					path1.append(uid);
					addPath(visited, uid, path1);

					StringBuilder path2 = new StringBuilder();
					path2.append(uid);
					UniqueId parentUID = parent.getUniqueId();
					path2.append(" <- ").append(parentUID);
					addPath(visited, parentUID, path2);

					String msg = String.format("Test %s exists in at least two paths:", uid) + "\n\t" + path1.toString()
							+ "\n\t" + path2.toString();
					return Optional.of(msg);
				}
				else {
					visited.put(uid, Optional.of(parent.getUniqueId()));
				}
				if (child.isContainer()) {
					queue.add(child);
				}
			}
		}
		return Optional.empty();
	}

	private static void addPath(HashMap<UniqueId, Optional<UniqueId>> visited, UniqueId from, StringBuilder path) {
		UniqueId current = from;

		while (visited.containsKey(current)) {
			Optional<UniqueId> backTraced = visited.get(current);
			if (backTraced.isPresent()) {
				path.append(" <- ").append(backTraced.get());
				current = backTraced.get();
			}
			else {
				break;
			}
		}
	}

}
