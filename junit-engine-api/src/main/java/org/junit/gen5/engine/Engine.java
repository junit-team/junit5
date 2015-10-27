package org.junit.gen5.engine;

import java.util.List;

// TODO Decide on name for Engine
public interface Engine {

	String getId();

	List<TestDescriptor> discoverTests(String className);

	void execute(List<TestDescriptor> testDescriptions) throws Exception;
}
