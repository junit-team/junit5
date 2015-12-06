package org.junit.gen5.engine.junit5ext;

import org.junit.gen5.engine.MutableTestDescriptor;
import org.junit.gen5.engine.TestPlanSpecification;

import java.util.List;

/**
 * A {@link TestResolver} is responsible for resolving different kind of test representatives.
 * Each resolver can contribute to the list of children of the given parent. It may only resolve
 * children, that accomplish the {@link TestPlanSpecification}. The children are returned as list.
 */
@FunctionalInterface
public interface TestResolver {
    List<MutableTestDescriptor> resolveFor(MutableTestDescriptor parent, TestPlanSpecification testPlanSpecification);
}
