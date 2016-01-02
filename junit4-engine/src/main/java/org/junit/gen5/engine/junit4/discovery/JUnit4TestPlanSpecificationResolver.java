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
import org.junit.gen5.engine.junit4.descriptor.RunnerDescriptor;
import org.junit.internal.builders.JUnit4Builder;
import org.junit.runner.Description;
import org.junit.runner.Runner;

public class JUnit4TestPlanSpecificationResolver {

	private EngineDescriptor engineDescriptor;

	public JUnit4TestPlanSpecificationResolver(EngineDescriptor engineDescriptor) {
		this.engineDescriptor = engineDescriptor;
	}

	public void resolve(TestPlanSpecification specification) {
		specification.accept(new TestPlanSpecificationElementVisitor() {

			@Override
			public void visitClass(Class<?> testClass) {
				Runner runner = new JUnit4Builder().safeRunnerForClass(testClass);

				RunnerDescriptor runnerDescriptor = new RunnerDescriptor(engineDescriptor, runner);
				addChildrenRecursively(runnerDescriptor);

				engineDescriptor.addChild(runnerDescriptor);
			}

			private void addChildrenRecursively(JUnit4TestDescriptor parent) {
				for (Description description : parent.getDescription().getChildren()) {
					JUnit4TestDescriptor child = new JUnit4TestDescriptor(parent, description);
					parent.addChild(child);
					addChildrenRecursively(child);
				}
			}
		});
	}

}
