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

import org.opentestalliance.TestAbortedException;
import org.opentestalliance.TestSkippedException;

public interface TestExecutor {
	void setTestExecutorRegistry(TestExecutorRegistry testExecutorRegistry);

	boolean canExecute(ExecutionContext context);

	void execute(ExecutionContext context) throws TestSkippedException, TestAbortedException, AssertionError;
}