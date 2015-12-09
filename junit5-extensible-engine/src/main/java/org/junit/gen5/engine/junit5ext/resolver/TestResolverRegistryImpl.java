
package org.junit.gen5.engine.junit5ext.resolver;

import java.util.LinkedList;
import java.util.List;

import org.junit.gen5.engine.MutableTestDescriptor;
import org.junit.gen5.engine.TestPlanSpecification;

public class TestResolverRegistryImpl implements TestResolverRegistry {
	private List<TestResolver> testResolvers = new LinkedList<>();

	@Override
	public void notifyResolvers(MutableTestDescriptor parent, TestPlanSpecification testPlanSpecification) {
		for (TestResolver testResolver : testResolvers) {
			List<MutableTestDescriptor> tests = testResolver.resolveFor(parent, testPlanSpecification);
			tests.forEach(test -> notifyResolvers(test, testPlanSpecification));
		}
	}

	@Override
	public void register(TestResolver testResolver) {
		testResolvers.add(testResolver);
	}
}
