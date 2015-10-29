package org.junit.gen5.engine.junit5.task;

import java.util.*;


public class CompositeTask implements ExecutionTask {


	private List<ExecutionTask> children;


	public CompositeTask(List<ExecutionTask> children) {
		this.children = children;


	}

	@Override
	public void execute() throws Exception {

		for (ExecutionTask child : this.children) {
			child.execute();
		}

	}




}
