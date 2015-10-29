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

//inspired by http://stackoverflow.com/questions/4965335/how-to-print-binary-tree-diagram
public class TaskPrinter {

	public void print(ExecutionTask task) {
		print("", true, task);

	}

	private void print(String prefix, boolean isTail, ExecutionTask task) {
		System.out.println(prefix + (isTail ? "└── " : "├── ") + task.toString());

		List<ExecutionTask> children = task.getChildren();

		if (children == null)
			return;

		for (int i = 0; i < children.size() - 1; i++) {
			print(prefix + (isTail ? "    " : "│   "), false, children.get(i));
		}
		if (children.size() > 0) {
			print(prefix + (isTail ? "    " : "│   "), true, children.get(children.size() - 1));
		}
	}

}