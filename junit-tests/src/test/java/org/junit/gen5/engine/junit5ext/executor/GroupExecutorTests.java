/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5ext.executor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.gen5.engine.junit5ext.executor.ExecutionContext.contextForDescriptor;

import org.junit.Before;
import org.junit.Test;
import org.junit.gen5.engine.junit5ext.descriptor.GroupDescriptor;
import org.junit.gen5.engine.junit5ext.testdoubles.TestDescriptorStub;
import org.junit.gen5.engine.junit5ext.testdoubles.TestExecutorRegistrySpy;

public class GroupExecutorTests {
	private TestExecutorRegistrySpy testExecutorRegistrySpy = new TestExecutorRegistrySpy();
	private GroupDescriptor emptyGroupDescriptor = new GroupDescriptor("id", "name");
	private GroupDescriptor singleGroupDescriptor = new GroupDescriptor("id", "name");
	private GroupExecutor groupExecutor = new GroupExecutor();

	@Before
	public void setUp() throws Exception {
		groupExecutor.setTestExecutorRegistry(testExecutorRegistrySpy);
		singleGroupDescriptor.addChild(emptyGroupDescriptor);
	}

	@Test
	public void givenAnArbitraryDescriptor_executorDeclinesRequest() throws Exception {
		boolean result = groupExecutor.canExecute(contextForDescriptor(new TestDescriptorStub()).build());
		assertThat(result).isFalse();
	}

	@Test
	public void givenAGroupDescriptor_executorAcceptsRequest() throws Exception {
		boolean result = groupExecutor.canExecute(contextForDescriptor(emptyGroupDescriptor).build());
		assertThat(result).isTrue();
	}

	@Test
	public void givenGroupWithoutChildren_noFurtherExecutionIsTriggered() throws Exception {
		groupExecutor.execute(contextForDescriptor(emptyGroupDescriptor).build());
		assertThat(testExecutorRegistrySpy.testDescriptors).isEmpty();
	}

	@Test
	public void givenGroupWithOneChild_childExecutionIsTriggered() throws Exception {
		groupExecutor.execute(contextForDescriptor(singleGroupDescriptor).build());
		assertThat(testExecutorRegistrySpy.testDescriptors).contains(emptyGroupDescriptor);
	}
}
