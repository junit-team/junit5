
package org.junit.gen5.engine.junit5;

import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.engine.AbstractTestDescriptor;
import org.junit.gen5.engine.Context;
import org.junit.gen5.engine.Parent;

public class JUnit5ClassDescriptor extends AbstractTestDescriptor implements Parent {

	private final Class<?> testClass;

	protected JUnit5ClassDescriptor(String engineId, Class<?> testClass) {
		super(engineId + ":" + testClass.getName());
		this.testClass = testClass;
	}

	@Override
	public String getDisplayName() {
		return testClass.getName();
	}

	@Override
	public boolean isTest() {
		return false;
	}

	@Override
	public boolean isContainer() {
		return true;
	}

	@Override
	public Context beforeAll(Context context) {
		return context.with("TestInstanceProvider", testInstanceProvider());
	}

	@Override
	public Context afterAll(Context context) {
		return context;
	}

	private TestInstanceProvider testInstanceProvider() {
		return () -> ReflectionUtils.newInstance(testClass);
	}

}
