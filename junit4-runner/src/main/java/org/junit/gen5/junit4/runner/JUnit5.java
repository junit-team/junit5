/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.junit4.runner;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

import java.lang.reflect.Method;
import java.util.ArrayList;
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
 * JUnit4-based {@link Runner} which runs tests that use the JUnit5
 * programming and extension models.
 *
 * <p>Annotating a test class with {@code @RunWith(JUnit5.class)} allows
 * it to be run with IDEs and build systems that support JUnit 4 but do
 * not yet support the JUnit 5 APIs directly.
 *
 * <p>Consult the various annotations in this package for configuration
 * options.
 *
 * @since 5.0
 * @see Classes
 * @see ClassNamePattern
 * @see Packages
 * @see UniqueIds
 * @see OnlyIncludeTags
 * @see ExcludeTags
 * @see OnlyEngine
 */
public class JUnit5 extends Runner {

	public static final String CREATE_SPECIFICATION_METHOD_NAME = "createSpecification";

	private static final Class<?>[] EMPTY_CLASS_ARRAY = new Class<?>[0];
	private static final String[] EMPTY_STRING_ARRAY = new String[0];
	private static final String EMPTY_STRING = "";

	private final Launcher launcher = new Launcher();
	private final Class<?> testClass;
	private final TestPlanSpecification specification;
	private final JUnit5TestTree testTree;

	public JUnit5(Class<?> testClass) throws InitializationError {
		this.testClass = testClass;
		this.specification = createSpecification();
		this.testTree = generateTestTree(testClass);
	}

	@Override
	public Description getDescription() {
		return this.testTree.getSuiteDescription();
	}

	@Override
	public void run(RunNotifier notifier) {
		Result result = new Result();
		notifier.addFirstListener(result.createListener());
		JUnit5RunnerListener listener = new JUnit5RunnerListener(this.testTree, notifier, result);
		this.launcher.registerTestExecutionListeners(listener);
		this.launcher.execute(this.specification);
	}

	private JUnit5TestTree generateTestTree(Class<?> testClass) {
		if (this.specification == null) {
			throw new IllegalStateException("TestPlanSpecification must not be null");
		}
		TestPlan plan = this.launcher.discover(this.specification);
		return new JUnit5TestTree(plan, testClass);
	}

	private TestPlanSpecification createSpecification() throws InitializationError {
		try {
			Method createSpecMethod = this.testClass.getMethod(CREATE_SPECIFICATION_METHOD_NAME);
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
		List<TestPlanSpecificationElement> specElements = new ArrayList<>();
		specElements.addAll(getClassSpecificationElements());
		specElements.addAll(getUniqueIdSpecificationElements());
		specElements.addAll(getPackageSpecificationElements());

		// Allows to simply add @RunWith(JUnit5.class) to any JUnit5 test case
		if (specElements.isEmpty()) {
			specElements.add(TestPlanSpecification.forClass(this.testClass));
		}

		TestPlanSpecification spec = TestPlanSpecification.build(specElements);
		addClassNameMatchesFilter(spec);
		addIncludeTagsFilter(spec);
		addExcludeTagsFilter(spec);
		addEngineIdFilter(spec);

		return spec;
	}

	private List<TestPlanSpecificationElement> getClassSpecificationElements() {
		return stream(getTestClasses()).map(TestPlanSpecification::forClass).collect(toList());
	}

	private List<TestPlanSpecificationElement> getUniqueIdSpecificationElements() {
		return stream(getUniqueIds()).map(TestPlanSpecification::forUniqueId).collect(toList());
	}

	private List<TestPlanSpecificationElement> getPackageSpecificationElements() {
		return stream(getPackageNames()).map(TestPlanSpecification::forPackage).collect(toList());
	}

	private void addClassNameMatchesFilter(TestPlanSpecification plan) {
		String regex = getClassNameRegExPattern();
		if (!regex.isEmpty()) {
			EngineFilter classNameFilter = TestPlanSpecification.classNameMatches(regex);
			plan.filterWith(classNameFilter);
		}
	}

	private void addIncludeTagsFilter(TestPlanSpecification plan) {
		String[] includeTags = getIncludeTags();
		if (includeTags.length > 0) {
			Predicate<TestDescriptor> tagNamesFilter = TestPlanSpecification.byTags(includeTags);
			plan.filterWith(tagNamesFilter);
		}
	}

	private void addExcludeTagsFilter(TestPlanSpecification plan) {
		String[] excludeTags = getExcludeTags();
		if (excludeTags.length > 0) {
			Predicate<TestDescriptor> excludeTagsFilter = TestPlanSpecification.excludeTags(excludeTags);
			plan.filterWith(excludeTagsFilter);
		}
	}

	private void addEngineIdFilter(TestPlanSpecification plan) {
		String engineId = getExplicitEngineId();
		if (StringUtils.isNotBlank(engineId)) {
			Predicate<TestDescriptor> engineFilter = TestPlanSpecification.byEngine(engineId);
			plan.filterWith(engineFilter);
		}
	}

	private Class<?>[] getTestClasses() {
		Classes annotation = this.testClass.getAnnotation(Classes.class);
		return (annotation != null ? annotation.value() : EMPTY_CLASS_ARRAY);
	}

	private String[] getUniqueIds() {
		UniqueIds annotation = this.testClass.getAnnotation(UniqueIds.class);
		return (annotation != null ? annotation.value() : EMPTY_STRING_ARRAY);
	}

	private String[] getPackageNames() {
		Packages annotation = this.testClass.getAnnotation(Packages.class);
		return (annotation != null ? annotation.value() : EMPTY_STRING_ARRAY);
	}

	private String[] getIncludeTags() {
		OnlyIncludeTags annotation = this.testClass.getAnnotation(OnlyIncludeTags.class);
		return (annotation != null ? annotation.value() : EMPTY_STRING_ARRAY);
	}

	private String[] getExcludeTags() {
		ExcludeTags annotation = this.testClass.getAnnotation(ExcludeTags.class);
		return (annotation != null ? annotation.value() : EMPTY_STRING_ARRAY);
	}

	private String getExplicitEngineId() {
		OnlyEngine annotation = this.testClass.getAnnotation(OnlyEngine.class);
		return (annotation != null ? annotation.value().trim() : EMPTY_STRING);
	}

	private String getClassNameRegExPattern() {
		ClassNamePattern annotation = this.testClass.getAnnotation(ClassNamePattern.class);
		return (annotation != null ? annotation.value().trim() : EMPTY_STRING);
	}

}
