package org.junit.gen5.engine.junit5ext.resolver;

import org.junit.gen5.engine.MutableTestDescriptor;
import org.junit.gen5.engine.TestPlanSpecification;
import org.junit.gen5.engine.junit5ext.resolver.TestResolver;

public interface TestResolverRegistry {
	void notifyResolvers(MutableTestDescriptor parent, TestPlanSpecification testPlanSpecification);

	void register(TestResolver testResolver);
}
