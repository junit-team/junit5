
package org.junit.gen5.engine.junit5ext.executor;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.gen5.engine.MutableTestDescriptor;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestPlanSpecification;
import org.junit.gen5.engine.junit5ext.resolver.TestResolver;
import org.junit.gen5.engine.junit5ext.resolver.TestResolverRegistry;

// TODO This class should become some kind of "JUnit" component, that will be initialized during start up
public class TestExecutorRegistryImpl implements TestExecutorRegistry {
	private List<TestExecutor> testExecutors = new LinkedList<>();

	@Override
	public List<TestExecutor> lookupExecutors(TestDescriptor testDescriptor) {
		return testExecutors.stream()
				.filter(testExecutor -> testExecutor.canExecute(testDescriptor))
				.collect(Collectors.toList());
	}

	@Override
	public void register(TestExecutor testExecutor) {
			testExecutors.add(testExecutor);
		}
}
