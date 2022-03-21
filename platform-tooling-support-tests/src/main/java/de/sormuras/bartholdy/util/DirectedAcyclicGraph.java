/*
 * Copyright 2015-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package de.sormuras.bartholdy.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class DirectedAcyclicGraph {

	private static final class Node implements Comparable<Node> {

		final String id;
		final Set<Node> next;

		Node(String id) {
			this.id = id;
			this.next = new TreeSet<>();
		}

		@Override
		public int compareTo(Node o) {
			return id.compareTo(o.id);
		}

		@Override
		public String toString() {
			return id;
		}
	}

	private final Map<String, Node> nodes;

	public DirectedAcyclicGraph() {
		this.nodes = new HashMap<>();
	}

	public boolean addEdge(String sourceId, String targetId) {
		// trivial cycle check
		if (sourceId.equals(targetId)) {
			throw new CycleDetectedException("Same node: " + sourceId + " == " + targetId);
		}
		// create new nodes, if necessary
		var before = nodes.size();
		var source = nodes.computeIfAbsent(sourceId, Node::new);
		var target = nodes.computeIfAbsent(targetId, Node::new);

		// detect potential cycle when no new node was created
		if (nodes.size() == before) {
			// edge already here...
			if (source.next.contains(target)) {
				return false;
			}
			// find direct cycle...
			if (target.next.contains(source)) {
				throw new CycleDetectedException("Anti-edge: " + source + " <-> " + target);
			}
			// find cyclic path...
			walk(source, target, List.of());
		}
		// remember node's connections
		source.next.add(target);
		return true;
	}

	private void walk(Node source, Node root, List<Node> path) {
		for (var node : root.next) {
			if (node == source) {
				var message = "From " + source + " over " + path + " and " + root + " back to " + source;
				throw new CycleDetectedException(message);
			}
			var newPath = new ArrayList<>(path);
			newPath.add(root);
			walk(source, node, List.copyOf(newPath));
		}
	}
}
