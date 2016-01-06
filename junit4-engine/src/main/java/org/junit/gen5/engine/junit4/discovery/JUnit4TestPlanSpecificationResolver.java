/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit4.discovery;

import org.junit.gen5.engine.EngineDescriptor;
import org.junit.gen5.engine.TestPlanSpecification;
import org.junit.gen5.engine.TestPlanSpecificationElementVisitor;
import org.junit.gen5.engine.junit4.descriptor.JUnit4TestDescriptor;
import org.junit.gen5.engine.junit4.descriptor.RunnerTestDescriptor;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runners.model.RunnerBuilder;

public class JUnit4TestPlanSpecificationResolver {

	private final EngineDescriptor engineDescriptor;

	public JUnit4TestPlanSpecificationResolver(EngineDescriptor engineDescriptor) {
		this.engineDescriptor = engineDescriptor;
	}

	public void resolve(TestPlanSpecification specification) {
		RunnerBuilder runnerBuilder = new DefensiveAllDefaultPossibilitiesBuilder();
		specification.getClasses().forEach((testClass) -> {
			Runner runner = runnerBuilder.safeRunnerForClass(testClass);
			if (runner != null) {
				engineDescriptor.addChild(createCompleteRunnerTestDescriptor(testClass, runner));
			}
		});
	}

	private RunnerTestDescriptor createCompleteRunnerTestDescriptor(Class<?> testClass, Runner runner) {
		RunnerTestDescriptor runnerTestDescriptor = new RunnerTestDescriptor(engineDescriptor, testClass, runner);
		addChildrenRecursively(runnerTestDescriptor);
		return runnerTestDescriptor;
	}

	private void addChildrenRecursively(JUnit4TestDescriptor parent) {
		for (Description description : parent.getDescription().getChildren()) {
			JUnit4TestDescriptor child = new JUnit4TestDescriptor(parent, description);
			// TODO #40 Ensure children are unique, i.e. generate different unique IDs for
			// Descriptions that are equal
			parent.addChild(child);
			addChildrenRecursively(child);
		}
	}

}
