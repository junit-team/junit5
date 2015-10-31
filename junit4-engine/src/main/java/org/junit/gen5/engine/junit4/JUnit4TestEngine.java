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

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.engine.ClassNameSpecification;
import org.junit.gen5.engine.EngineExecutionContext;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestEngine;
import org.junit.gen5.engine.TestPlanSpecification;
import org.junit.gen5.engine.TestPlanSpecificationElement;
import org.junit.internal.runners.ErrorReportingRunner;
import org.junit.runner.Description;
import org.junit.runner.Request;
import org.junit.runner.Runner;

public class JUnit4TestEngine implements TestEngine {

	@Override
	public String getId() {
		return "junit4";
	}

	@Override
	public Collection<TestDescriptor> discoverTests(TestPlanSpecification specification,
			TestDescriptor engineDescriptor) {
		Set<TestDescriptor> result = new LinkedHashSet<>();
		for (TestPlanSpecificationElement element : specification) {
			if (element instanceof ClassNameSpecification) {
				String className = ((ClassNameSpecification) element).getClassName();
				Class<?> testClass = ReflectionUtils.loadClass(className);
				Runner runner = Request.aClass(testClass).getRunner();

				// TODO This skips malformed JUnit 4 tests, too
				if (!(runner instanceof ErrorReportingRunner)) {
					Description root = runner.getDescription();
					DescriptionTestDescriptor rootDescriptor = new DescriptionTestDescriptor(engineDescriptor, root);
					addRecursively(rootDescriptor, result);
				}
			}
		}
		return result;
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
		context.getTestDescriptions().stream()
			.filter(TestDescriptor::isTest)
			.map(testDescriptor -> (DescriptionTestDescriptor) testDescriptor)
			.forEach(testDescriptor -> executeSingleTest(context, testDescriptor));
		//@formatter:on
	}

	private void executeSingleTest(EngineExecutionContext context, DescriptionTestDescriptor testDescriptor) {
		context.getTestExecutionListener().testStarted(testDescriptor);
		context.getTestExecutionListener().testFailed(testDescriptor, new RuntimeException("not executed, yet"));
	}

}
