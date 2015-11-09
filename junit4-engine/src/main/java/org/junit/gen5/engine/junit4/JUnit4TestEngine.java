/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit4;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.engine.ClassNameSpecification;
import org.junit.gen5.engine.EngineDescriptor;
import org.junit.gen5.engine.EngineExecutionContext;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestEngine;
import org.junit.gen5.engine.TestPlanSpecification;
import org.junit.gen5.engine.TestPlanSpecificationElement;
import org.junit.internal.runners.ErrorReportingRunner;
import org.junit.runner.Description;
import org.junit.runner.Request;
import org.junit.runner.RunWith;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.notification.RunNotifier;

public class JUnit4TestEngine implements TestEngine {

	@Override
	public String getId() {
		return "junit4";
	}

	@Override
	public void discoverTests(TestPlanSpecification specification, EngineDescriptor engineDescriptor) {
		//Todo: result is no longer needed
		Set<TestDescriptor> result = new LinkedHashSet<>();
		for (TestPlanSpecificationElement element : specification) {
			if (element instanceof ClassNameSpecification) {
				String className = ((ClassNameSpecification) element).getClassName();

				Class<?> testClass = ReflectionUtils.loadClass(className).orElseThrow(
					() -> new IllegalArgumentException("Class " + className + " not found."));

				//JL: Hack to break endless recursion if runner will lead to the
				// execution of JUnit5 test (eg. @RunWith(JUnit5.class))
				// how to do that properly?
				if (testClass.isAnnotationPresent(RunWith.class)) {
					continue;
				}
				Runner runner = Request.aClass(testClass).getRunner();

				// TODO This skips malformed JUnit 4 tests, too
				if (!(runner instanceof ErrorReportingRunner)) {
					DescriptionTestDescriptor rootDescriptor = new RunnerTestDescriptor(engineDescriptor, runner);
					addRecursively(rootDescriptor, result);
				}
			}
		}
	}

	private void addRecursively(DescriptionTestDescriptor parent, Set<TestDescriptor> result) {
		result.add(parent);
		for (Description child : parent.getDescription().getChildren()) {
			addRecursively(new DescriptionTestDescriptor(parent, child), result);
		}
	}

	@Override
	public boolean supports(TestDescriptor testDescriptor) {
		return testDescriptor instanceof DescriptionTestDescriptor;
	}

	@Override
	public void execute(EngineExecutionContext context) {
		//@formatter:off
		Map<RunnerTestDescriptor, List<DescriptionTestDescriptor>> groupedByRunner = context.getEngineDescriptor().allChildren().stream()
			.filter(testDescriptor -> !(testDescriptor instanceof RunnerTestDescriptor))
			.map(testDescriptor -> (DescriptionTestDescriptor) testDescriptor)
			.collect(groupingBy(testDescriptor -> findRunner(testDescriptor)));
		//@formatter:on

		for (Entry<RunnerTestDescriptor, List<DescriptionTestDescriptor>> entry : groupedByRunner.entrySet()) {
			RunnerTestDescriptor runnerTestDescriptor = entry.getKey();
			List<DescriptionTestDescriptor> testDescriptors = entry.getValue();
			try {
				executeSingleRunnerSafely(context, runnerTestDescriptor, testDescriptors);
			}
			catch (Exception e) {
				context.getTestExecutionListener().testFailed(runnerTestDescriptor, e);
			}
		}
	}

	private void executeSingleRunnerSafely(EngineExecutionContext context, RunnerTestDescriptor runnerTestDescriptor,
			List<DescriptionTestDescriptor> testDescriptors) throws NoTestsRemainException {
		Runner runner = runnerTestDescriptor.getRunner();

		//@formatter:off
		Map<Description, DescriptionTestDescriptor> description2descriptor = testDescriptors.stream()
			.collect(toMap(DescriptionTestDescriptor::getDescription, identity()));
		//@formatter:on

		Filter filter = new ActiveDescriptionsFilter(description2descriptor.keySet());
		filter.apply(runner);

		RunNotifier notifier = new RunNotifier();
		notifier.addListener(new RunListenerAdapter(description2descriptor, context.getTestExecutionListener()));

		runner.run(notifier);
	}

	private RunnerTestDescriptor findRunner(TestDescriptor testDescriptor) {
		if (testDescriptor instanceof RunnerTestDescriptor) {
			return (RunnerTestDescriptor) testDescriptor;
		}
		return findRunner(testDescriptor.getParent());
	}

}
