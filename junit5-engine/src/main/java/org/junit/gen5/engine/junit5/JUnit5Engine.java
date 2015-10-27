package org.junit.gen5.engine.junit5;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.junit.gen5.engine.Engine;
import org.junit.gen5.engine.EngineTestDescription;

public class JUnit5Engine implements Engine {

	private static final String JUNIT5_ENGINE_ID = "junit5";

	@Override
	public List<EngineTestDescription> discoverTests(String className) {
		return Arrays.<EngineTestDescription> asList(new JavaMethodTestDescription(className, "failingTest"),
				new JavaMethodTestDescription(className, "succeedingTest"));
	}

	@Override
	public void execute(List<EngineTestDescription> testDescriptions) throws Exception {
		for (EngineTestDescription testDescription : testDescriptions) {
			execute(testDescription);
		}
	}

	private void execute(EngineTestDescription testDescription) throws Exception {
		if (testDescription instanceof JavaMethodTestDescription) {
			JavaMethodTestDescription javaMethodTestDescription = (JavaMethodTestDescription) testDescription;
			String className = javaMethodTestDescription.getClassName();
			// TODO use correct classloader
			Class<?> testClass = Class.forName(className);
			Constructor<?> constructor = testClass.getDeclaredConstructor();
			constructor.setAccessible(true);
			Object testInstance = constructor.newInstance();
			Method method = testClass.getDeclaredMethod(javaMethodTestDescription.getMethodName());
			method.setAccessible(true);
			try {
				method.invoke(testInstance);
			} catch (Exception e) {
				System.out.println("Test failed: " + javaMethodTestDescription.getMethodName());
				e.printStackTrace(System.out);
			}
		}
	}

	@Override
	public String getId() {
		return JUNIT5_ENGINE_ID;
	}

}