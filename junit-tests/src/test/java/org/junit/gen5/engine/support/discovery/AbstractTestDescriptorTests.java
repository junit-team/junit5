/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.support.discovery;

import static org.junit.gen5.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.gen5.api.BeforeEach;
import org.junit.gen5.api.Test;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.UniqueId;
import org.junit.gen5.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.gen5.engine.support.descriptor.EngineDescriptor;

public class AbstractTestDescriptorTests {

	EngineDescriptor engineDescriptor;

	@BeforeEach
	public void initTree() {
		engineDescriptor = new EngineDescriptor(UniqueId.forEngine("testEngine"), "testEngine");
		GroupDescriptor group1 = new GroupDescriptor(UniqueId.root("group", "group1"));
		engineDescriptor.addChild(group1);
		GroupDescriptor group2 = new GroupDescriptor(UniqueId.root("group", "group2"));
		engineDescriptor.addChild(group2);
		GroupDescriptor group11 = new GroupDescriptor(UniqueId.root("group", "group1-1"));
		group1.addChild(group11);

		group1.addChild(new LeafDescriptor(UniqueId.root("leaf", "leaf1-1")));
		group1.addChild(new LeafDescriptor(UniqueId.root("leaf", "leaf1-2")));

		group2.addChild(new LeafDescriptor(UniqueId.root("leaf", "leaf2-1")));

		group11.addChild(new LeafDescriptor(UniqueId.root("leaf", "leaf11-1")));
	}

	@Test
	public void visitAllNodes() {
		List<TestDescriptor> visited = new ArrayList<>();
		engineDescriptor.accept(visited::add);

		assertEquals(8, visited.size());
	}

	@Test
	public void pruneLeaf() {
		TestDescriptor.Visitor visitor = descriptor -> {
			if (descriptor.getUniqueId().equals(UniqueId.root("leaf", "leaf1-1")))
				descriptor.removeFromHierarchy();
		};
		engineDescriptor.accept(visitor);

		List<UniqueId> visited = new ArrayList<>();
		engineDescriptor.accept(descriptor -> visited.add(descriptor.getUniqueId()));

		assertEquals(7, visited.size());
		assertTrue(visited.contains(UniqueId.root("group", "group1")));
		assertFalse(visited.contains(UniqueId.root("leaf", "leaf1-1")));
	}

	@Test
	public void pruneGroup() {
		final AtomicInteger countVisited = new AtomicInteger();
		TestDescriptor.Visitor visitor = descriptor -> {
			if (descriptor.getUniqueId().equals(UniqueId.root("group", "group1")))
				descriptor.removeFromHierarchy();
			countVisited.incrementAndGet();
		};
		engineDescriptor.accept(visitor);

		assertEquals(4, countVisited.get(), "Children of pruned element are not visited");

		List<UniqueId> visited = new ArrayList<>();
		engineDescriptor.accept(descriptor -> visited.add(descriptor.getUniqueId()));

		assertEquals(3, visited.size());
		assertFalse(visited.contains(UniqueId.root("group", "group1")));
	}

}

class GroupDescriptor extends AbstractTestDescriptor {

	GroupDescriptor(UniqueId uniqueId) {
		super(uniqueId);
	}

	@Override
	public String getName() {
		return getUniqueId().getUniqueString();
	}

	@Override
	public String getDisplayName() {
		return "group: " + getName();
	}

	@Override
	public boolean isTest() {
		return false;
	}

	@Override
	public boolean isContainer() {
		return true;
	}
}

class LeafDescriptor extends AbstractTestDescriptor {

	LeafDescriptor(UniqueId uniqueId) {
		super(uniqueId);
	}

	@Override
	public String getName() {
		return getUniqueId().getUniqueString();
	}

	@Override
	public String getDisplayName() {
		return "leave: " + getName();
	}

	@Override
	public boolean isTest() {
		return true;
	}

	@Override
	public boolean isContainer() {
		return false;
	}
}
