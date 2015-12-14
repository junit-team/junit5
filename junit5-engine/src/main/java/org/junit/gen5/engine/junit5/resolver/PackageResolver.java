package org.junit.gen5.engine.junit5.resolver;

import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestPlanSpecification;

public class PackageResolver extends JUnit5TestResolver {
    @Override
    public TestResolverResult resolveFor(TestDescriptor parent, TestPlanSpecification testPlanSpecification) {
        return TestResolverResult.empty();
    }
}
