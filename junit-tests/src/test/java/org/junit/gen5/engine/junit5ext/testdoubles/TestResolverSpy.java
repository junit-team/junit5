package org.junit.gen5.engine.junit5ext.testdoubles;

import org.junit.gen5.engine.MutableTestDescriptor;
import org.junit.gen5.engine.TestPlanSpecification;
import org.junit.gen5.engine.junit5ext.resolver.TestResolver;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class TestResolverSpy implements TestResolver {
    public List<TestResolverRequest> resolvedFor = new LinkedList<>();

    @Override
    public List<MutableTestDescriptor> resolveFor(MutableTestDescriptor parent, TestPlanSpecification testPlanSpecification) {
        resolvedFor.add(new TestResolverRequest(parent, testPlanSpecification));
        return Collections.emptyList();
    }
}
