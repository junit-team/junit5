package org.junit.gen5.engine.junit5ext.testdoubles;

import org.junit.gen5.engine.MutableTestDescriptor;
import org.junit.gen5.engine.TestPlanSpecification;

public class TestResolverRequest {
    public MutableTestDescriptor parent;
    public TestPlanSpecification testPlanSpecification;

    public TestResolverRequest(MutableTestDescriptor parent, TestPlanSpecification testPlanSpecification) {
        this.parent = parent;
        this.testPlanSpecification = testPlanSpecification;
    }
}
