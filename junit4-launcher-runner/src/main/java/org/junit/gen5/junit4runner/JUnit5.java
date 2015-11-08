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

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

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
		Class<?>[]value();
	}

	private static Class<?>[] getAnnotatedClasses(Class<?> testClass) throws InitializationError {
		Classes annotation = testClass.getAnnotation(Classes.class);
		if (annotation == null) {
			return new Class[0];
		}
		return annotation.value();
	}

	/**
	 * The <code>UniqueIds</code> annotation specifies unique ids of classes or methods to be run when a class
	 * annotated with <code>@RunWith(JUnit5.class)</code> is run.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	@Inherited
	public @interface UniqueIds {

		/**
		 * @return the classes to be run
		 */
		String[]value();
	}

	private static String[] getAnnotatedUniqueIds(Class<?> testClass) throws InitializationError {
		UniqueIds annotation = testClass.getAnnotation(UniqueIds.class);
		if (annotation == null) {
			return new String[0];
		}
		return annotation.value();
	}

	/**
	 * The <code>Packages</code> annotation specifies names of packages to be run when a class
	 * annotated with <code>@RunWith(JUnit5.class)</code> is run.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	@Inherited
	public @interface Packages {

		/**
		 * @return the classes to be run
		 */
		String[]value();
	}

	private static String[] getAnnotatedPackages(Class<?> testClass) throws InitializationError {
		Packages annotation = testClass.getAnnotation(Packages.class);
		if (annotation == null) {
			return new String[0];
		}
		return annotation.value();
	}

	/**
	 * The <code>Packages</code> annotation specifies names of packages to be run when a class
	 * annotated with <code>@RunWith(JUnit5.class)</code> is run.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	@Inherited
	public @interface OnlyIncludeTags {

		/**
		 * @return the classes to be run
		 */
		String[]value();
	}

	private static String[] getAnnotatedOnlyIncludeTags(Class<?> testClass) throws InitializationError {
		OnlyIncludeTags annotation = testClass.getAnnotation(OnlyIncludeTags.class);
		if (annotation == null) {
			return new String[0];
		}
		return annotation.value();
	}

	private final Class<?> testClass;
	private TestPlanSpecification specification = null;
	private Description description;
	private final Launcher launcher = new Launcher();
	private final Map<TestDescriptor, Description> descriptions = new HashMap<>();

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
			List<TestPlanSpecificationElement> specs = getClassnameSpecificationElements();
			specs.addAll(getUniqueIdSpecificationElements());
			specs.addAll(getPackagesSpecificationElements());
			if (specs.isEmpty()) { //Allows to simply add @RunWith(JUnit5.class) to any JUnit5 test case
				specs.add(TestPlanSpecification.forClassName(testClass.getName()));
			}
			TestPlanSpecification plan = TestPlanSpecification.build(specs);
			String[] onlyIncludeTags = getAnnotatedOnlyIncludeTags(testClass);
			if (onlyIncludeTags.length > 0) {
				Predicate<TestDescriptor> tagNamesFilter = TestPlanSpecification.filterByTags(onlyIncludeTags);
				plan.filterWith(tagNamesFilter);
			}
			return plan;
		}
		catch (Exception e) {
			throw new InitializationError(e);
		}
	}

	private Collection<TestPlanSpecificationElement> getPackagesSpecificationElements() throws InitializationError {
		String[] packages = getAnnotatedPackages(testClass);
		return stream(packages).map(TestPlanSpecification::forPackage).collect(toList());
	}

	private List<TestPlanSpecificationElement> getClassnameSpecificationElements() throws InitializationError {
		Class<?>[] testClasses = getAnnotatedClasses(testClass);
		return stream(testClasses).map(Class::getName).map(TestPlanSpecification::forClassName).collect(toList());
	}

	private List<TestPlanSpecificationElement> getUniqueIdSpecificationElements() throws InitializationError {
		String[] uniqueIds = getAnnotatedUniqueIds(testClass);
		return stream(uniqueIds).map(TestPlanSpecification::forUniqueId).collect(toList());
	}

	private Description generateDescription() {
		Description suiteDescription = Description.createSuiteDescription(testClass.getName());
		if (specification != null) {
			TestPlan plan = launcher.discover(specification);
			buildDescriptionTree(suiteDescription, plan);
		}
		return suiteDescription;
	}

	private void buildDescriptionTree(Description suiteDescription, TestPlan plan) {
		//Todo: If children come before their parent the tree is not correctly built up
		for (TestDescriptor descriptor : plan.getTestDescriptors()) {
			addDescriptionFor(descriptor, suiteDescription);
		}
	}

	private Description addDescriptionFor(TestDescriptor descriptor, Description root) {
		if (descriptions.containsKey(descriptor))
			return descriptions.get(descriptor);
		Description newDescription = createJUnit4Description(descriptor);
		descriptions.put(descriptor, newDescription);
		if (descriptor.getParent() == null) {
			root.addChild(newDescription);
		}
		else {
			Description parent = addDescriptionFor(descriptor.getParent(), root);
			parent.addChild(newDescription);
		}
		return newDescription;
	}

	private Description createJUnit4Description(TestDescriptor testDescriptor) {
		if (testDescriptor.isTest()) {
			return Description.createTestDescription(testDescriptor.getParent().getDisplayName(),
				testDescriptor.getDisplayName(), testDescriptor.getUniqueId());
		}
		else {
			return Description.createSuiteDescription(testDescriptor.getDisplayName(), testDescriptor.getUniqueId());
		}
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
			notifier.fireTestFinished(description);
		}

		@Override
		public void testAborted(TestDescriptor testDescriptor, Throwable t) {
			Description description = findJUnit4Description(testDescriptor);
			notifier.fireTestAssumptionFailed(new Failure(description, t));
			notifier.fireTestFinished(description);
		}

		@Override
		public void testFailed(TestDescriptor testDescriptor, Throwable t) {
			Description description = findJUnit4Description(testDescriptor);
			notifier.fireTestFailure(new Failure(description, t));
			notifier.fireTestFinished(description);
		}

		@Override
		public void testSucceeded(TestDescriptor testDescriptor) {
			Description description = findJUnit4Description(testDescriptor);
			notifier.fireTestFinished(description);
		}

		private Description findJUnit4Description(TestDescriptor testDescriptor) {
			Description description = descriptions.get(testDescriptor.getUniqueId());
			if (description == null) {
				description = addDescriptionFor(testDescriptor, getDescription());
			}
			return description;
		}

	}
}
