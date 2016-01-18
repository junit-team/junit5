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

import static org.junit.gen5.api.Assertions.assertEquals;
import static org.junit.gen5.api.Assertions.assertFalse;
import static org.junit.gen5.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.gen5.api.BeforeEach;
import org.junit.gen5.api.Test;
import org.junit.gen5.engine.DiscoveryRequest;
import org.junit.gen5.engine.EngineAwareTestDescriptor;
import org.junit.gen5.engine.ExecutionRequest;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestEngine;

public class AbstractTestDescriptorTests {

	EngineDescriptor engineDescriptor;

	@BeforeEach
	public void initTree() {
		engineDescriptor = new EngineDescriptor(new TestEngine() {

			@Override
			public EngineAwareTestDescriptor discoverTests(DiscoveryRequest discoveryRequest) {
				return engineDescriptor;
			}

			@Override
			public void execute(ExecutionRequest request) {
			}

			@Override
			public String getId() {
				return "testEngine";
			}
		});
		GroupDescriptor group1 = new GroupDescriptor("group1");
		engineDescriptor.addChild(group1);
		GroupDescriptor group2 = new GroupDescriptor("group2");
		engineDescriptor.addChild(group2);
		GroupDescriptor group11 = new GroupDescriptor("group1-1");
		group1.addChild(group11);

		group1.addChild(new LeafDescriptor("leaf1-1"));
		group1.addChild(new LeafDescriptor("leaf1-2"));

		group2.addChild(new LeafDescriptor("leaf2-1"));

		group11.addChild(new LeafDescriptor("leaf11-1"));
	}

	@Test
	public void visitAllNodes() {
		List<TestDescriptor> visited = new ArrayList<>();
		engineDescriptor.accept((descriptor, delete) -> visited.add(descriptor));

		assertEquals(8, visited.size());
	}

	@Test
	public void pruneLeaf() {
		TestDescriptor.Visitor visitor = (TestDescriptor descriptor, Runnable delete) -> {
			if (descriptor.getUniqueId().equals("leaf1-1"))
				delete.run();
		};
		engineDescriptor.accept(visitor);

		List<String> visited = new ArrayList<>();
		engineDescriptor.accept((descriptor, delete) -> visited.add(descriptor.getUniqueId()));

		assertEquals(7, visited.size());
		assertTrue(visited.contains("group1"));
		assertFalse(visited.contains("leaf1-1"));
	}

	@Test
	public void pruneGroup() {
		final AtomicInteger countVisited = new AtomicInteger();
		TestDescriptor.Visitor visitor = (descriptor, delete) -> {
			if (descriptor.getUniqueId().equals("group1"))
				delete.run();
			countVisited.incrementAndGet();
		};
		engineDescriptor.accept(visitor);

		assertEquals(4, countVisited.get(), "Children of pruned element are not visited");

		List<String> visited = new ArrayList<>();
		engineDescriptor.accept((descriptor, delete) -> visited.add(descriptor.getUniqueId()));

		assertEquals(3, visited.size());
		assertFalse(visited.contains("group1"));
	}

}

class GroupDescriptor extends AbstractTestDescriptor {

	GroupDescriptor(String uniqueId) {
		super(uniqueId);
	}

	@Override
	public String getName() {
		return getUniqueId();
	}

	@Override
	public String getDisplayName() {
		return "group: " + getUniqueId();
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

	LeafDescriptor(String uniqueId) {
		super(uniqueId);
	}

	@Override
	public String getName() {
		return getUniqueId();
	}

	@Override
	public String getDisplayName() {
		return "leave: " + getUniqueId();
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
