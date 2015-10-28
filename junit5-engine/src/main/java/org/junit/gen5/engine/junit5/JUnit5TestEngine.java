package org.junit.gen5.engine.junit5;

import static java.lang.String.*;
import static java.util.stream.Collectors.*;
import static org.junit.gen5.commons.util.ReflectionUtils.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.gen5.api.After;
import org.junit.gen5.api.Before;
import org.junit.gen5.api.Test;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestEngine;
import org.junit.gen5.engine.TestExecutionListener;
import org.junit.gen5.engine.TestPlanSpecification;
import org.opentestalliance.TestAbortedException;
import org.opentestalliance.TestSkippedException;

public class JUnit5TestEngine implements TestEngine {

	// TODO - SBE - could be replaced by JUnit5TestEngine.class.getCanonicalName()
	private static final String ENGINE_ID = "junit5";

	@Override
	public String getId() {
		return ENGINE_ID;
	}

	@Override
	public List<TestDescriptor> discoverTests(TestPlanSpecification specification) {
		List<Class<?>> testClasses = discoverTestClasses(specification);

		List<TestDescriptor> testDescriptors = testClasses.stream()
			.map(Class::getDeclaredMethods)
			.flatMap(Arrays::stream)
			.filter(method -> method.isAnnotationPresent(Test.class))
			.map(method -> new JavaTestDescriptor(getId(), method))
			.collect(toList());

		testDescriptors.addAll(
			specification.getUniqueIds().stream()
				.map(JavaTestDescriptor::from)
				.collect(toList())
		);

		return testDescriptors;
	}

	private List<Class<?>> discoverTestClasses(TestPlanSpecification testPlanSpecification) {
		List<Class<?>> testClasses = new ArrayList<>();

		// Add specified test classes directly
		testClasses.addAll(testPlanSpecification.getClasses());

		// Add test classes by name
		for (String className : testPlanSpecification.getClassNames()) {
			try {
				testClasses.add(Class.forName(className));
			} catch (ClassNotFoundException e) {
				throw new IllegalArgumentException(format("Failed to load test class '%s'", className));
			}
		}

		return testClasses;
	}

	@Override
	public boolean supports(TestDescriptor testDescriptor) {
		return testDescriptor instanceof JavaTestDescriptor;
	}

	@Override
	public void execute(Collection<TestDescriptor> testDescriptors, TestExecutionListener testExecutionListener) {
		for (TestDescriptor testDescriptor : testDescriptors) {
			try {
				testExecutionListener.testStarted(testDescriptor);

				JavaTestDescriptor javaTestDescriptor = (JavaTestDescriptor) testDescriptor;
				executeTest(javaTestDescriptor);
				testExecutionListener.testSucceeded(testDescriptor);
			}
			catch (InvocationTargetException ex) {
				Throwable targetException = ex.getTargetException();
				if (targetException instanceof TestSkippedException) {
					testExecutionListener.testSkipped(testDescriptor, targetException);
				}
				else if (targetException instanceof TestAbortedException) {
					testExecutionListener.testAborted(testDescriptor, targetException);
				}
				else {
					testExecutionListener.testFailed(testDescriptor, targetException);
				}
			} catch (NoSuchMethodException | InstantiationException | IllegalAccessException e) {
				throw new IllegalStateException(
					String.format("Test %s is not well-formed and cannot be executed", testDescriptor.getUniqueId()));
			} catch (Exception ex) {
				testExecutionListener.testFailed(testDescriptor, ex);
			}
		}
	}

	protected void executeTest(JavaTestDescriptor javaTestDescriptor) throws Exception {
		Class<?> testClass = javaTestDescriptor.getTestClass();

		// TODO Extract test instantiation
		Object testInstance = newInstance(testClass);

		executeBeforeMethods(testClass, testInstance);
		invokeMethod(javaTestDescriptor.getTestMethod(), testInstance);
		executeAfterMethods(testClass, testInstance);
	}

	private void executeBeforeMethods(Class<?> testClass, Object testInstance) throws Exception {
		for (Method method: findAnnotatedMethods(testClass, Before.class)) {
			invokeMethod(method, testInstance);
		}
	}

	private void executeAfterMethods(Class<?> testClass, Object testInstance) throws Exception {
		for (Method method : findAnnotatedMethods(testClass, After.class)) {
			invokeMethod(method, testInstance);
		}
	}

	private List<Method> findAnnotatedMethods(Class<?> testClass, Class<? extends Annotation> annotationType) {
		return Arrays.stream(testClass.getDeclaredMethods())
			.filter(method -> method.isAnnotationPresent(annotationType))
			.collect(toList());
	}

}
