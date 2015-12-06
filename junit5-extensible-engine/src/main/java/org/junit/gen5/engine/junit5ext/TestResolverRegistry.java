package org.junit.gen5.engine.junit5ext;

import org.junit.gen5.engine.MutableTestDescriptor;
import org.junit.gen5.engine.TestPlanSpecification;

public interface TestResolverRegistry {
	void notifyResolvers(MutableTestDescriptor parent, TestPlanSpecification testPlanSpecification);

	void register(TestResolver testResolver);
}
