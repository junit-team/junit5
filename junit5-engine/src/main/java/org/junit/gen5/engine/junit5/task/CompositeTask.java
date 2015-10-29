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

public class CompositeTask implements ExecutionTask {

	private List<ExecutionTask> children;

	private String label;


	public CompositeTask(List<ExecutionTask> children, String label) {
		this.children = children;
		this.label = label;
	}

	@Override
	public void execute() throws Exception {

		System.out.println("--> TASK: " + this.getClass().getSimpleName() + ": '" + this.label + "' - children count: "
				+ this.children.size());

		for (ExecutionTask child : this.children) {
			child.execute();
		}

	}

}
