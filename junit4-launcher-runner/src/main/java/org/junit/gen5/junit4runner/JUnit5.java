/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.junit4runner;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestPlanSpecification;
import org.junit.gen5.engine.TestPlanSpecificationElement;
import org.junit.gen5.launcher.Launcher;
import org.junit.gen5.launcher.TestPlan;
import org.junit.gen5.launcher.TestPlanExecutionListener;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;

public class JUnit5 extends Runner {

	public static final String CREATE_SPECIFICATION_METHOD_NAME = "createSpecification";

	/**
	 * The <code>Classes</code> annotation specifies the classes to be run when a class
	 * annotated with <code>@RunWith(JUnit5.class)</code> is run.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	@Inherited
	public @interface Classes {

		/**
		 * @return the classes to be run
		 */
		public Class<?>[]value();
	}

	private static Class<?>[] getAnnotatedClasses(Class<?> testClass) throws InitializationError {
		Classes annotation = testClass.getAnnotation(Classes.class);
		if (annotation == null) {
			throw new InitializationError(String.format("class '%s' must have a @%s annotation or a static %s method",
				testClass.getName(), Classes.class.getSimpleName(), CREATE_SPECIFICATION_METHOD_NAME));
		}
		return annotation.value();
	}

	private final Class<?> testClass;
	private TestPlanSpecification specification = null;
	private Description description;
	private final Launcher launcher = new Launcher();
	private final Map<String, Description> id2Description = new HashMap<>();

	public JUnit5(Class<?> testClass) throws InitializationError {
		this.testClass = testClass;
		this.specification = createSpecification();
		description = generateDescription();
	}

	private TestPlanSpecification createSpecification() throws InitializationError {
		try {
			Method createSpecMethod = testClass.getMethod(CREATE_SPECIFICATION_METHOD_NAME);
			return (TestPlanSpecification) createSpecMethod.invoke(null);
		}
		catch (NoSuchMethodException nsme) {
			Class<?>[] testClasses = getAnnotatedClasses(testClass);
			List<TestPlanSpecificationElement> testClassNames = Arrays.stream(testClasses).map(
				clazz -> TestPlanSpecification.forClassName(clazz.getName())).collect(Collectors.toList());
			return TestPlanSpecification.build(testClassNames);
		}
		catch (Exception e) {
			throw new InitializationError(e);
		}
	}

	private Description generateDescription() {
		Description suiteDescription = Description.createSuiteDescription(
			"JUnit 5 test suite: " + testClass.getSimpleName());
		if (specification != null) {
			TestPlan plan = launcher.discover(specification);
			for (TestDescriptor descriptor : plan.getTestDescriptors()) {
				Description description = createJUnit4Description(descriptor, suiteDescription);
				id2Description.put(descriptor.getUniqueId(), description);
			}
		}
		return suiteDescription;
	}

	private Description createJUnit4Description(TestDescriptor testDescriptor, Description rootDescription) {
		Description newDescription = null;
		if (testDescriptor.isTest()) {
			newDescription = Description.createTestDescription(testDescriptor.getParent().getDisplayName(),
				testDescriptor.getDisplayName(), testDescriptor.getUniqueId());
		}
		else {
			newDescription = Description.createSuiteDescription(testDescriptor.getDisplayName(),
				testDescriptor.getUniqueId());
		}
		if (testDescriptor.getParent() == null) {
			rootDescription.addChild(newDescription);
		}
		else {
			Description parent = id2Description.get(testDescriptor.getParent().getUniqueId());
			if (parent != null) {
				parent.addChild(newDescription);
			}
		}
		return newDescription;

	}

	@Override
	public Description getDescription() {
		return description;
	}

	@Override
	public void run(RunNotifier notifier) {
		Result result = new Result();
		notifier.addFirstListener(result.createListener());
		RunnerListener listener = new RunnerListener(notifier, result);
		launcher.registerTestPlanExecutionListeners(listener);
		launcher.execute(specification);
	}

	private class RunnerListener implements TestPlanExecutionListener {

		private final RunNotifier notifier;
		private final Result result;

		RunnerListener(RunNotifier notifier, Result result) {
			this.notifier = notifier;
			this.result = result;
		}

		@Override
		public void testPlanExecutionStarted(TestPlan testPlan) {
			notifier.fireTestRunStarted(description);
		}

		@Override
		public void testPlanExecutionFinished(TestPlan testPlan) {
			notifier.fireTestRunFinished(result);
		}

		@Override
		public void dynamicTestFound(TestDescriptor testDescriptor) {
			System.out.println("JUnit5 test runner cannot handle dynamic tests");
		}

		@Override
		public void testStarted(TestDescriptor testDescriptor) {
			Description description = findJUnit4Description(testDescriptor);
			notifier.fireTestStarted(description);
		}

		@Override
		public void testSkipped(TestDescriptor testDescriptor, Throwable t) {
			Description description = findJUnit4Description(testDescriptor);
			// TODO We call this after calling fireTestStarted. This leads to a wrong test
			// count in Eclipse.
			notifier.fireTestIgnored(description);
		}

		@Override
		public void testAborted(TestDescriptor testDescriptor, Throwable t) {
			Description description = findJUnit4Description(testDescriptor);
			notifier.fireTestAssumptionFailed(new Failure(description, t));
		}

		@Override
		public void testFailed(TestDescriptor testDescriptor, Throwable t) {
			Description description = findJUnit4Description(testDescriptor);
			notifier.fireTestFailure(new Failure(description, t));

		}

		@Override
		public void testSucceeded(TestDescriptor testDescriptor) {
			Description description = findJUnit4Description(testDescriptor);
			notifier.fireTestFinished(description);
		}

		private Description findJUnit4Description(TestDescriptor testDescriptor) {
			Description description = id2Description.get(testDescriptor.getUniqueId());
			if (description == null) {
				description = createJUnit4Description(testDescriptor, description);
			}
			return description;
		}

	}
}
