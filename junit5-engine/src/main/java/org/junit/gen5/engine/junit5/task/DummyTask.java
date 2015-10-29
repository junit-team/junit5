/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.task;

import java.util.*;

public class DummyTask implements ExecutionTask {

	private String label;


	public DummyTask(String label) {
		this.label = label;
	}

	@Override
	public void execute() throws Exception {

		System.out.println("--> TASK: " + this.label);

	}

	@Override
	public List<ExecutionTask> getChildren() {
		return null;
	}

}
