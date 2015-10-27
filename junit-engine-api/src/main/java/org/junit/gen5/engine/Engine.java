package org.junit.gen5.engine;

import java.util.List;

// TODO Decide on name for Engine
public interface Engine {

	String getId();

	List<TestDescriptor> discoverTests(TestPlan testPlan);

	void execute(List<TestDescriptor> testDescriptions) throws Throwable;
}
