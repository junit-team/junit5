package org.junit.gen5.engine.junit5.task;

public class DummyTask implements ExecutionTask {


	private String label;

	public DummyTask(String label) {
		this.label = label;
	}


	@Override
	public void execute() throws Exception {

		System.out.println("--> TASK: " + this.label);

	}



}
