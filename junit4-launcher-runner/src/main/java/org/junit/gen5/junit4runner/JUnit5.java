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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import org.junit.gen5.commons.util.StringUtils;
import org.junit.gen5.engine.EngineFilter;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestPlanSpecification;
import org.junit.gen5.engine.TestPlanSpecificationElement;
import org.junit.gen5.launcher.Launcher;
import org.junit.gen5.launcher.TestPlan;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;

/**
 * @since 5.0
 */
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

		Class<?>[]value();
	}

	private static Class<?>[] getAnnotatedClasses(Class<?> testClass) {
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

		String[]value();
	}

	private static String[] getAnnotatedUniqueIds(Class<?> testClass) {
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

		String[]value();
	}

	private static String[] getAnnotatedPackages(Class<?> testClass) {
		Packages annotation = testClass.getAnnotation(Packages.class);
		if (annotation == null) {
			return new String[0];
		}
		return annotation.value();
	}

	/**
	 * The <code>OnlyIncludeTags</code> annotation specifies tag to be filtered when a class
	 * annotated with <code>@RunWith(JUnit5.class)</code> is run.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	@Inherited
	public @interface OnlyIncludeTags {

		String[]value();
	}

	private static String[] getAnnotatedOnlyIncludeTags(Class<?> testClass) {
		OnlyIncludeTags annotation = testClass.getAnnotation(OnlyIncludeTags.class);
		if (annotation == null) {
			return new String[0];
		}
		return annotation.value();
	}

	/**
	 * The <code>OnlyEngine</code> annotation specifies the engine ID to be filtered when a class
	 * annotated with <code>@RunWith(JUnit5.class)</code> is run.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	@Inherited
	public @interface OnlyEngine {

		/**
		 * @return engineId
		 */
		String value();
	}

	private static String getAnnotatedOnlyEngine(Class<?> testClass) {
		OnlyEngine annotation = testClass.getAnnotation(OnlyEngine.class);
		if (annotation == null) {
			return "";
		}
		return annotation.value();
	}

	/**
	 * The <code>OnlyEngine</code> annotation specifies the engine ID to be filtered when a class
	 * annotated with <code>@RunWith(JUnit5.class)</code> is run.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	@Inherited
	public @interface ClassNameMatches {

		/**
		 * @return regex
		 */
		String value();
	}

	private static String getAnnotatedClassNameMatches(Class<?> testClass) {
		ClassNameMatches annotation = testClass.getAnnotation(ClassNameMatches.class);
		if (annotation == null) {
			return "";
		}
		return annotation.value();
	}

	private final Class<?> testClass;
	private final TestPlanSpecification specification;
	private final JUnit5TestTree testTree;
	private final Launcher launcher = new Launcher();

	public JUnit5(Class<?> testClass) throws InitializationError {
		this.testClass = testClass;
		this.specification = createSpecification();
		this.testTree = generateTestTree(testClass);
	}

	protected JUnit5TestTree generateTestTree(Class<?> testClass) {
		if (specification != null) {
			TestPlan plan = launcher.discover(specification);
			return new JUnit5TestTree(plan, testClass);
		}
		return testTree;
	}

	private TestPlanSpecification createSpecification() throws InitializationError {
		try {
			Method createSpecMethod = testClass.getMethod(CREATE_SPECIFICATION_METHOD_NAME);
			return (TestPlanSpecification) createSpecMethod.invoke(null);
		}
		catch (NoSuchMethodException notUsed) {
			return createSpecificationFromAnnotations();
		}
		catch (Exception e) {
			throw new InitializationError(e);
		}
	}

	private TestPlanSpecification createSpecificationFromAnnotations() {
		List<TestPlanSpecificationElement> specs = new ArrayList<>();
		specs.addAll(getClassnameSpecificationElements());
		specs.addAll(getUniqueIdSpecificationElements());
		specs.addAll(getPackagesSpecificationElements());

		//Allows to simply add @RunWith(JUnit5.class) to any JUnit5 test case
		if (specs.isEmpty()) {
			specs.add(TestPlanSpecification.forClassName(testClass.getName()));
		}

		TestPlanSpecification plan = TestPlanSpecification.build(specs);
		addOnlyIncludeTagsFilter(plan);
		addOnlyIncludeEngineFilter(plan);
		addClassNameMatches(plan);

		return plan;
	}

	private void addClassNameMatches(TestPlanSpecification plan) {
		String regex = getAnnotatedClassNameMatches(testClass).trim();
		if (!regex.isEmpty()) {
			EngineFilter classNameFilter = TestPlanSpecification.classNameMatches(regex);
			plan.filterWith(classNameFilter);
		}
	}

	private void addOnlyIncludeTagsFilter(TestPlanSpecification plan) {
		String[] onlyIncludeTags = getAnnotatedOnlyIncludeTags(testClass);
		if (onlyIncludeTags.length > 0) {
			Predicate<TestDescriptor> tagNamesFilter = TestPlanSpecification.byTags(onlyIncludeTags);
			plan.filterWith(tagNamesFilter);
		}
	}

	private void addOnlyIncludeEngineFilter(TestPlanSpecification plan) {
		String onlyIncludeEngine = getAnnotatedOnlyEngine(testClass);
		if (StringUtils.isNotBlank(onlyIncludeEngine)) {
			Predicate<TestDescriptor> engineFilter = TestPlanSpecification.byEngine(onlyIncludeEngine);
			plan.filterWith(engineFilter);
		}
	}

	private Collection<TestPlanSpecificationElement> getPackagesSpecificationElements() {
		String[] packages = getAnnotatedPackages(testClass);
		return stream(packages).map(TestPlanSpecification::forPackage).collect(toList());
	}

	private List<TestPlanSpecificationElement> getClassnameSpecificationElements() {
		Class<?>[] testClasses = getAnnotatedClasses(testClass);
		return stream(testClasses).map(Class::getName).map(TestPlanSpecification::forClassName).collect(toList());
	}

	private List<TestPlanSpecificationElement> getUniqueIdSpecificationElements() {
		String[] uniqueIds = getAnnotatedUniqueIds(testClass);
		return stream(uniqueIds).map(TestPlanSpecification::forUniqueId).collect(toList());
	}

	@Override
	public Description getDescription() {
		return testTree.getSuiteDescription();
	}

	@Override
	public void run(RunNotifier notifier) {
		Result result = new Result();
		notifier.addFirstListener(result.createListener());
		JUnit5RunnerListener listener = new JUnit5RunnerListener(testTree, notifier, result);
		launcher.registerTestPlanExecutionListeners(listener);
		launcher.execute(specification);
	}

}
