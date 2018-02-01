/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.support.hierarchical;

import java.util.concurrent.RecursiveAction;

import org.junit.platform.commons.util.ExceptionUtils;
import org.junit.platform.engine.support.hierarchical.HierarchicalTestExecutorService.TestTask;

// this class cannot not be serialized because TestTask is not Serializable
@SuppressWarnings("serial")
class ExclusiveTask extends RecursiveAction {

	private final TestTask testTask;

	ExclusiveTask(TestTask testTask) {
		this.testTask = testTask;
	}

	@SuppressWarnings("try")
	@Override
	public void compute() {
		try (AcquiredResourceLock acquiredLock = testTask.getResourceLock().acquire()) {
			testTask.execute();
		}
		catch (InterruptedException e) {
			ExceptionUtils.throwAsUncheckedException(e);
		}
	}
}
