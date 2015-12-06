package org.junit.gen5.engine.junit5ext.resolver;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.junit.gen5.commons.util.ObjectUtils;
import org.junit.gen5.engine.MutableTestDescriptor;
import org.junit.gen5.engine.TestPlanSpecification;
import org.junit.gen5.engine.TestPlanSpecificationElementVisitor;
import org.junit.gen5.engine.junit5ext.testable.ClassTestGroup;

public class JavaClassResolver implements TestResolver {
    @Override
    public List<MutableTestDescriptor> resolveFor(MutableTestDescriptor parent, TestPlanSpecification testPlanSpecification) {
        ObjectUtils.verifyNonNull(parent, "Parent must not be null!");
        ObjectUtils.verifyNonNull(testPlanSpecification, "TestPlanSpecification must not be null!");

        if (parent.isRoot()) {
            return resolveAllClassesFromSpecification(parent, testPlanSpecification);
        } else {
            return Collections.emptyList();
        }
    }

    private List<MutableTestDescriptor> resolveAllClassesFromSpecification(MutableTestDescriptor parent, TestPlanSpecification testPlanSpecification) {
        List<MutableTestDescriptor> result = new LinkedList<>();

        testPlanSpecification.accept(new TestPlanSpecificationElementVisitor() {
            @Override
            public void visitClass(Class<?> testClass) {
                result.add(getTestGroupForClass(parent, testClass));
            }
        });

        return result;
    }

    private MutableTestDescriptor getTestGroupForClass(MutableTestDescriptor parent, Class<?> testClass) {
        String parentUniqueId = parent.getUniqueId();
        String uniqueId = String.format("%s:%s", parentUniqueId, testClass.getCanonicalName());
        String displayName = testClass.getSimpleName();

        ClassTestGroup classTestGroup = new ClassTestGroup(testClass, uniqueId, displayName);
        classTestGroup.setParent(parent);
        return classTestGroup;
    }
}
