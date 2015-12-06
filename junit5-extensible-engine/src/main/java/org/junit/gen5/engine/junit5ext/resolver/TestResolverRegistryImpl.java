
package org.junit.gen5.engine.junit5ext.resolver;

import org.junit.gen5.engine.MutableTestDescriptor;
import org.junit.gen5.engine.TestPlanSpecification;
import org.junit.gen5.engine.junit5ext.resolver.TestResolver;
import org.junit.gen5.engine.junit5ext.resolver.TestResolverRegistry;

import java.util.LinkedList;
import java.util.List;

public class TestResolverRegistryImpl implements TestResolverRegistry {
	private List<TestResolver> testResolvers = new LinkedList<>();

	@Override
	public void notifyResolvers(MutableTestDescriptor parent, TestPlanSpecification testPlanSpecification) {
		for (TestResolver testResolver : testResolvers) {
			testResolver.resolveFor(parent, testPlanSpecification);
		}
	}

	@Override
	public void register(TestResolver testResolver) {
		testResolvers.add(testResolver);
	}
}
