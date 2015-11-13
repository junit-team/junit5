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

import static java.util.function.Function.*;
import static java.util.stream.Collectors.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.gen5.engine.ClassFilter;
import org.junit.gen5.engine.EngineDescriptor;
import org.junit.gen5.engine.EngineExecutionContext;
import org.junit.gen5.engine.EngineFilter;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestEngine;
import org.junit.gen5.engine.TestPlanSpecification;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.notification.RunNotifier;

public class JUnit4TestEngine implements TestEngine {

	@Override
	public String getId() {
		return JUnit4TestDescriptor.ENGINE_ID;
	}

	@Override
	public void discoverTests(TestPlanSpecification specification, EngineDescriptor engineDescriptor) {
		JUnit4SpecificationResolver resolver = new JUnit4SpecificationResolver(engineDescriptor);
		specification.accept(resolver);
		applyEngineFilters(specification.getEngineFilters(), engineDescriptor);
	}

	// More or less copied over from JUnit5TestEngine
	private void applyEngineFilters(List<EngineFilter> engineFilters, EngineDescriptor engineDescriptor) {
		//Todo: Currently only works with a single ClassFilter
		if (engineFilters.isEmpty())
			return;
		ClassFilter filter = (ClassFilter) engineFilters.get(0);

		TestDescriptor.Visitor filteringVisitor = (descriptor, remove) -> {
			if (descriptor instanceof DescriptionTestDescriptor) {
				DescriptionTestDescriptor descriptionTestDescriptor = (DescriptionTestDescriptor) descriptor;
				descriptionTestDescriptor.getTestClass().ifPresent(clazz -> {
					if (!filter.acceptClass(clazz))
						remove.run();
				});
			}
		};

		engineDescriptor.accept(filteringVisitor);
	}

	@Override
	public void execute(EngineExecutionContext context) {

		// TODO Use capabilities of engine node to build up tree or to visit nodes
		List<TestDescriptor> originalTestDescriptors = new ArrayList<>(context.getEngineDescriptor().allChildren());

		//@formatter:off
		Map<RunnerTestDescriptor, List<DescriptionTestDescriptor>> groupedByRunner = originalTestDescriptors.stream()
			.filter(testDescriptor -> (testDescriptor instanceof DescriptionTestDescriptor))
			.map(testDescriptor -> (DescriptionTestDescriptor) testDescriptor)
			.collect(groupingBy(testDescriptor -> findRunnerTestDescriptor(testDescriptor)));
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

	private RunnerTestDescriptor findRunnerTestDescriptor(JUnit4TestDescriptor testDescriptor) {
		if (testDescriptor instanceof RunnerTestDescriptor) {
			return (RunnerTestDescriptor) testDescriptor;
		}
		if (testDescriptor instanceof DescriptionTestDescriptor) {
			return findRunnerTestDescriptor((JUnit4TestDescriptor) testDescriptor.getParent().get());
		}
		throw new IllegalStateException("Cannot handle testDescriptor of class " + testDescriptor.getClass().getName());
	}

}
