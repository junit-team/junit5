/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestEngine;
import org.junit.gen5.engine.TestExecutionContext;
import org.junit.gen5.engine.TestExecutor;

/**
 * @author Stefan Bechtold
 * @since 5.0
 */
@Data
@EqualsAndHashCode(of = { "uniqueId" })
public class EngineTestGroup implements TestDescriptor, TestExecutor {

	private final TestEngine engine;

	public EngineTestGroup(TestEngine engine) {
		this.engine = engine;
	}

	@Override
	public String getUniqueId() {
		return engine.getId();
	}

	@Override
	public String getDisplayName() {
		return engine.getId();
	}

	@Override
	public TestDescriptor getParent() {
		return null;
	}

	@Override
	public boolean isTest() {
		return false;
	}

	@Override
	public void execute(TestExecutionContext context) {
		Stream<TestExecutor> testExecutorStream = context.getTestDescriptors().stream().filter(
			testDescriptor -> Objects.nonNull(testDescriptor.getParent())).filter(
				testDescriptor -> testDescriptor.getParent().equals(this)).map(TestExecutionResolver::forDescriptor);

		List<TestExecutor> testExecutors = testExecutorStream.collect(Collectors.toList());
		testExecutors.stream().forEach(testExecutor -> testExecutor.execute(context));
	}

	@Override
	public boolean isRoot() {
		return true;
	}
}