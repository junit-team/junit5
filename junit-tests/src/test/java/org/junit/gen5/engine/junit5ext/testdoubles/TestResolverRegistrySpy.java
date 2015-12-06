
package org.junit.gen5.engine.junit5ext.testdoubles;

import java.util.LinkedList;
import java.util.List;

import org.junit.gen5.engine.MutableTestDescriptor;
import org.junit.gen5.engine.TestPlanSpecification;
import org.junit.gen5.engine.junit5ext.TestResolver;
import org.junit.gen5.engine.junit5ext.TestResolverRegistry;

public class TestResolverRegistrySpy implements TestResolverRegistry {
	public List<TestResolverRequest> notifications = new LinkedList<>();
	public List<TestResolver> registeredTestResolvers = new LinkedList<>();

	@Override
	public void notifyResolvers(MutableTestDescriptor parent, TestPlanSpecification testPlanSpecification) {
		notifications.add(new TestResolverRequest(parent, testPlanSpecification));
	}

	@Override
	public void register(TestResolver testResolver) {
		registeredTestResolvers.add(testResolver);
	}
}