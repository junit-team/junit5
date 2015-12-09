package org.junit.gen5.engine.junit5ext;

import org.junit.gen5.engine.ExecutionRequest;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestEngine;
import org.junit.gen5.engine.TestPlanSpecification;
import org.junit.gen5.engine.junit5ext.resolver.TestResolver;
import org.junit.gen5.engine.junit5ext.resolver.TestResolverRegistry;
import org.junit.gen5.engine.junit5ext.descriptor.GroupDescriptor;
import org.junit.gen5.engine.junit5ext.resolver.TestResolverRegistryImpl;

import java.util.ServiceLoader;

public class ExtensibleJUnit5TestEngine implements TestEngine {
    public static final String ENGINE_ID = "junit5ext";
    public static final String DISPLAY_NAME = "JUnit5 Engine (extensible)";

    private TestResolverRegistry testResolverRegistry;

    @Override
    public String getId() {
        return ENGINE_ID;
    }

    @Override
    public String toString() {
        return DISPLAY_NAME;
    }

    @Override
    public void initialize() {
        // TODO this part must be triggered by the launcher/test engine before test discovery/execution starts
        testResolverRegistry = new TestResolverRegistryImpl();
        ServiceLoader.load(TestResolver.class).forEach(testResolver -> testResolverRegistry.register(testResolver));
    }

    @Override
    public TestDescriptor discoverTests(TestPlanSpecification specification) {
        GroupDescriptor root = new GroupDescriptor(getId(), toString());
        testResolverRegistry.notifyResolvers(root, specification);
        return root;
    }

    @Override
    public void execute(ExecutionRequest request) {
        throw new UnsupportedOperationException("Method has not been implemented, yet!");
    }

    public void setTestResolverRegistry(TestResolverRegistry testResolverRegistry) {
        this.testResolverRegistry = testResolverRegistry;
    }
}
