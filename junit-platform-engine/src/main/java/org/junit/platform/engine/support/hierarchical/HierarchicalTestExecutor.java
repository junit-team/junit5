/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.support.hierarchical;

import java.util.List;
import java.util.concurrent.Future;

import org.junit.platform.commons.annotation.ExecutionMode;
import org.junit.platform.commons.util.ExceptionUtils;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.engine.support.hierarchical.HierarchicalTestExecutorService.TestTask;

/**
 * Implementation core of all {@link TestEngine TestEngines} that wish to
 * use the {@link Node} abstraction as the driving principle for structuring
 * and executing test suites.
 *
 * <p>A {@code HierarchicalTestExecutor} is instantiated by a concrete
 * implementation of {@link HierarchicalTestEngine} and takes care of
 * executing nodes in the hierarchy in the appropriate order as well as
 * firing the necessary events in the {@link EngineExecutionListener}.
 *
 * @param <C> the type of {@code EngineExecutionContext} used by the
 * {@code HierarchicalTestEngine}
 * @since 1.0
 */
class HierarchicalTestExecutor<C extends EngineExecutionContext> {

	private static final SingleTestExecutor singleTestExecutor = new SingleTestExecutor();

	private final TestDescriptor rootTestDescriptor;
	private final EngineExecutionListener listener;
	private final C rootContext;
	private HierarchicalTestExecutorService<C> executorService;

	HierarchicalTestExecutor(ExecutionRequest request, C rootContext,
			HierarchicalTestExecutorService<C> executorService) {
		this.rootTestDescriptor = request.getRootTestDescriptor();
		this.listener = request.getEngineExecutionListener();
		this.rootContext = rootContext;
		this.executorService = executorService;
	}

	void execute() {
		NodeExecutor<C> rootNodeExecutor = createRootNodeExecutor();
		waitFor(executorService.submit(toTestTask(rootNodeExecutor, this.rootContext)));
	}

	protected NodeExecutor<C> createRootNodeExecutor() {
		NodeExecutor<C> rootNodeExecutor = new NodeExecutor<>(this.rootTestDescriptor);
		new NodeWalker(new LockManager()).walk(rootNodeExecutor);
		return rootNodeExecutor;
	}

	static void waitFor(Future<?> future) {
		try {
			future.get();
		}
		catch (Exception e) {
			// TODO do something sensible
			throw ExceptionUtils.throwAsUncheckedException(e);
		}
	}

	private TestTask<C> toTestTask(NodeExecutor<C> nodeExecutor, C context) {
		return new DefaultTestTask(nodeExecutor, context);
	}


	private class DefaultTestTask implements TestTask<C> {

		private final NodeExecutor<C> nodeExecutor;
		private final C context;

		DefaultTestTask(NodeExecutor<C> nodeExecutor, C context) {
			this.nodeExecutor = nodeExecutor;
			this.context = context;
		}

		@Override
		public TestDescriptor getTestDescriptor() {
			return nodeExecutor.getTestDescriptor();
		}

		@Override
		public C getParentExecutionContext() {
			return context;
		}

		@Override
		public void execute() {
			nodeExecutor.execute(this.context, listener, executorService, singleTestExecutor, DefaultTestTask::new);
		}

		@Override
		public List<ExclusiveResource> getExclusiveResources() {
			return nodeExecutor.getNode().getExclusiveResources();
		}

		@Override
		public ExecutionMode getExecutionMode() {
			return nodeExecutor.getNode().getExecutionMode();
		}
	}
}
