package org.junit.gen5.engine.junit5;

import static java.lang.String.*;
import static java.util.stream.Collectors.*;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.gen5.api.Test;
import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestEngine;
import org.junit.gen5.engine.TestExecutionListener;
import org.junit.gen5.engine.TestPlanSpecification;
import org.opentestalliance.TestAbortedException;
import org.opentestalliance.TestSkippedException;

public class JUnit5TestEngine implements TestEngine {

	// TODO Consider using class names for engine IDs.
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

		// TODO Build a tree of TestDescriptors.
		//
		// Simply iterating over a collection is insufficient for our purposes. We need a
		// tree (or some form of hierarchical data structure) in order to be able to
		// execute each test within the correct scope.
		//
		// For example, we need to execute all test methods within a given test class as a
		// group in order to:
		//
		// 1) retain the instance across test method invocations (if desired).
		// 2) invoke class-level before & after methods _around_ the set of methods.

		for (TestDescriptor testDescriptor : testDescriptors) {

			Preconditions.condition(testDescriptor instanceof JavaTestDescriptor,
				String.format("%s supports test descriptors of type %s, not of type %s", getClass().getSimpleName(),
					JavaTestDescriptor.class.getName(),
					(testDescriptor != null ? testDescriptor.getClass().getName() : "null")));

			JavaTestDescriptor javaTestDescriptor = (JavaTestDescriptor) testDescriptor;

			try {
				testExecutionListener.testStarted(javaTestDescriptor);
				new TestExecutor(javaTestDescriptor).execute();
				testExecutionListener.testSucceeded(javaTestDescriptor);
			}
			catch (InvocationTargetException ex) {
				Throwable targetException = ex.getTargetException();
				if (targetException instanceof TestSkippedException) {
					testExecutionListener.testSkipped(javaTestDescriptor, targetException);
				}
				else if (targetException instanceof TestAbortedException) {
					testExecutionListener.testAborted(javaTestDescriptor, targetException);
				}
				else {
					testExecutionListener.testFailed(javaTestDescriptor, targetException);
				}
			}
			catch (NoSuchMethodException | InstantiationException | IllegalAccessException ex) {
				throw new IllegalStateException(String.format("Test %s is not well-formed and cannot be executed",
					javaTestDescriptor.getUniqueId()), ex);
			}
			catch (Exception ex) {
				testExecutionListener.testFailed(javaTestDescriptor, ex);
			}
		}
	}

}
