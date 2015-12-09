package org.junit.gen5.engine.junit5.descriptor;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.gen5.api.extension.TestExecutionContext;
import org.junit.gen5.api.extension.TestExtension;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.junit5.JUnit5Context;

final class TestExecutionContextImpl implements TestExecutionContext {

	private final TestDescriptor testDescriptor;
	private final JUnit5Context context;

	public TestExecutionContextImpl(TestDescriptor testDescriptor, JUnit5Context context) {
		this.testDescriptor = testDescriptor;
		this.context = context;
	}

	@Override
	public Optional<Method> getTestMethod() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<Object> getTestInstance() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<Class<?>> getTestClass() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<TestExecutionContext> getParent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<TestExtension> getExtensions() {
		return context.getTestExtensionRegistry().getExtensions();
	}

	@Override
	public String getDisplayName() {
		return testDescriptor.getDisplayName();
	}

	@Override
	public Map<String, Object> getAttributes() {
		// TODO Auto-generated method stub
		return null;
	}
}