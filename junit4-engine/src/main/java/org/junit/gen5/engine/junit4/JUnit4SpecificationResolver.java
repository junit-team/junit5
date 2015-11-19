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

import java.io.File;

import lombok.Data;

import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.engine.EngineDescriptor;
import org.junit.gen5.engine.TestPlanSpecificationVisitor;
import org.junit.internal.runners.ErrorReportingRunner;
import org.junit.runner.Description;
import org.junit.runner.Request;
import org.junit.runner.RunWith;
import org.junit.runner.Runner;

@Data
class JUnit4SpecificationResolver implements TestPlanSpecificationVisitor {

	private static final IsJUnit4TestClassWithTests isJUnit4TestClassWithTests = new IsJUnit4TestClassWithTests();

	private final EngineDescriptor engineDescriptor;

	// TODO support more TestPlanSpecificationElements/visit methods

	@Override
	public void visitClassSpecification(Class<?> testClass) {

		// TODO JL: Hack to break endless recursion if runner will lead to the
		// execution of JUnit5 test (eg. @RunWith(JUnit5.class))
		// how to do that properly?
		if (testClass.isAnnotationPresent(RunWith.class)) {
			return;
		}

		Runner runner = Request.aClass(testClass).getRunner();

		// TODO This skips malformed JUnit 4 tests, too
		if (!(runner instanceof ErrorReportingRunner)) {
			RunnerTestDescriptor testDescriptor = new RunnerTestDescriptor(runner);
			engineDescriptor.addChild(testDescriptor);
			addRecursively(testDescriptor);
		}
	}

	@Override
	public void visitPackageSpecification(String packageName) {
		ReflectionUtils.findAllClassesInPackage(packageName, isJUnit4TestClassWithTests).stream().forEach(
			this::visitClassSpecification);
	}

	@Override
	public void visitAllTestsSpecification(File rootDirectory) {
		ReflectionUtils.findAllClassesInClasspathRoot(rootDirectory, isJUnit4TestClassWithTests).stream().forEach(
			this::visitClassSpecification);
	}

	private void addRecursively(JUnit4TestDescriptor parent) {
		for (Description child : parent.getDescription().getChildren()) {
			DescriptionTestDescriptor testDescriptor = new DescriptionTestDescriptor(child);
			parent.addChild(testDescriptor);
			addRecursively(testDescriptor);
		}
	}

}
