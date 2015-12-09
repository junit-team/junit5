package org.junit.gen5.engine.junit5ext.testdoubles;

import org.junit.gen5.engine.MutableTestDescriptor;
import org.junit.gen5.engine.TestPlanSpecification;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TestResolverSpyWithTestsForRoot extends TestResolverSpy {
    private final MutableTestDescriptor root;
    private final MutableTestDescriptor resolvedTest;

    public TestResolverSpyWithTestsForRoot(MutableTestDescriptor root) {
        this.root = root;
        this.resolvedTest = new MutableTestDescriptorStub(root);
    }

    public MutableTestDescriptor getResolvedTest() {
        return resolvedTest;
    }

    @Override
    public List<MutableTestDescriptor> resolveFor(MutableTestDescriptor parent, TestPlanSpecification testPlanSpecification) {
        super.resolveFor(parent, testPlanSpecification);

        if (root.equals(parent)) {
            return Arrays.asList(resolvedTest);
        } else {
            return Collections.emptyList();
        }
    }
}
