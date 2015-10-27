package org.junit.gen5.engine.junit5;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.gen5.api.Test;
import org.junit.gen5.engine.Engine;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestPlan;

public class JUnit5Engine implements Engine {

	private static final String JUNIT5_ENGINE_ID = "junit5";

	@Override
	public String getId() {
		return JUNIT5_ENGINE_ID;
	}

	@Override
	public List<TestDescriptor> discoverTests(TestPlan testPlan) {
		return testPlan.getClasses().stream().flatMap(clazz -> Arrays.stream(clazz.getDeclaredMethods()))
				// TODO Should be extensible
				.filter(method -> method.isAnnotationPresent(Test.class))
				.map(method -> JavaTestDescriptor.fromMethod(method, this))
				// .peek(d -> testPlan.getTestListeners().forEach(l ->
				// l.testFound(d)))
				.collect(Collectors.toList());
	}

	@Override
	public void execute(List<TestDescriptor> testDescriptions) throws Throwable {
		for (TestDescriptor testDescription : testDescriptions) {
			executeSingleMethod(testDescription);
		}
	}

	private void executeSingleMethod(TestDescriptor testDescriptor) throws Throwable {
		try {
			JavaTestDescriptor javaTestDescriptor = (JavaTestDescriptor) testDescriptor;
			Object testInstance = instantiateClass(javaTestDescriptor);
			invokeMethod(javaTestDescriptor, testInstance);
		} catch (InvocationTargetException e) {
			throw e.getTargetException();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new IllegalArgumentException(
					"Test not well-formed and cannot be executed! ID: " + testDescriptor.getId());
		}
	}

	private Object instantiateClass(JavaTestDescriptor javaTestDescriptor)
			throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
		Class<?> testClass = javaTestDescriptor.getTestClass();
		Constructor<?> constructor = testClass.getDeclaredConstructor();
		if (!constructor.isAccessible()) {
			constructor.setAccessible(true);
		}
		return constructor.newInstance();
	}

	private void invokeMethod(JavaTestDescriptor javaTestDescriptor, Object testInstance)
			throws IllegalAccessException, InvocationTargetException {
		Method method = javaTestDescriptor.getTestMethod();
		if (!method.isAccessible()) {
			method.setAccessible(true);
		}
		method.invoke(testInstance);
	}

}