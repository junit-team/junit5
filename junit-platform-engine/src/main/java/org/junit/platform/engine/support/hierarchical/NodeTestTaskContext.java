/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.support.hierarchical;

import org.junit.platform.engine.EngineExecutionListener;

/**
 * @since 1.3.1
 */
class NodeTestTaskContext {

	private final EngineExecutionListener listener;
	private final HierarchicalTestExecutorService executorService;
	private final ThrowableCollector.Factory throwableCollectorFactory;
	private final NodeExecutionAdvisor executionAdvisor;

	public NodeTestTaskContext(EngineExecutionListener listener, HierarchicalTestExecutorService executorService,
			ThrowableCollector.Factory throwableCollectorFactory, NodeExecutionAdvisor executionAdvisor) {
		this.listener = listener;
		this.executorService = executorService;
		this.throwableCollectorFactory = throwableCollectorFactory;
		this.executionAdvisor = executionAdvisor;
	}

	NodeTestTaskContext withListener(EngineExecutionListener listener) {
		if (this.listener == listener) {
			return this;
		}
		return new NodeTestTaskContext(listener, executorService, throwableCollectorFactory, executionAdvisor);
	}

	EngineExecutionListener getListener() {
		return listener;
	}

	HierarchicalTestExecutorService getExecutorService() {
		return executorService;
	}

	ThrowableCollector.Factory getThrowableCollectorFactory() {
		return throwableCollectorFactory;
	}

	NodeExecutionAdvisor getExecutionAdvisor() {
		return executionAdvisor;
	}
}
