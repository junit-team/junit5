package org.junit.gen5.engine.junit5;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import org.junit.gen5.engine.Engine;
import org.junit.gen5.engine.TestDescriptor;

public class JUnit5Engine implements Engine {

	private static final String JUNIT5_ENGINE_ID = "junit5";

	@Override
	public List<TestDescriptor> discoverTests(String className) {
		// TODO implement this
		return Collections.emptyList();
//		return Arrays.<TestDescriptor> asList(new JavaTestDescriptor(className, "failingTest"),
//				new JavaTestDescriptor(className, "succeedingTest"));
	}

	@Override
	public void execute(List<TestDescriptor> testDescriptions) throws Exception {
		for (TestDescriptor testDescription : testDescriptions) {
			execute(testDescription);
		}
	}

	private void execute(TestDescriptor testDescription) throws Exception {
		if (testDescription instanceof JavaTestDescriptor) {
			JavaTestDescriptor javaTestDescription = (JavaTestDescriptor) testDescription;
			Class<?> testClass = javaTestDescription.getTestClass();
			Constructor<?> constructor = testClass.getDeclaredConstructor();
			constructor.setAccessible(true);
			Object testInstance = constructor.newInstance();
			Method method = javaTestDescription.getTestMethod();
			method.setAccessible(true);
			try {
				method.invoke(testInstance);
			} catch (Exception e) {
				System.out.println("Test failed: " + method.getName());
				e.printStackTrace(System.out);
			}
		}
	}

	@Override
	public String getId() {
		return JUNIT5_ENGINE_ID;
	}

}