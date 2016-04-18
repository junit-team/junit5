/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.surefire;

import static org.apache.maven.surefire.report.SimpleReportEntry.ignored;
import static org.junit.gen5.engine.TestExecutionResult.Status.ABORTED;
import static org.junit.gen5.engine.TestExecutionResult.Status.FAILED;

import java.util.Optional;

import org.apache.maven.surefire.report.PojoStackTraceWriter;
import org.apache.maven.surefire.report.RunListener;
import org.apache.maven.surefire.report.SimpleReportEntry;
import org.apache.maven.surefire.report.StackTraceWriter;
import org.junit.gen5.engine.TestExecutionResult;
import org.junit.gen5.engine.support.descriptor.JavaSource;
import org.junit.gen5.launcher.TestExecutionListener;
import org.junit.gen5.launcher.TestIdentifier;

final class RunListenerAdapter implements TestExecutionListener {

	private RunListener runListener;

	public RunListenerAdapter(RunListener reporter) {
		this.runListener = reporter;
	}

	@Override
	public void executionStarted(TestIdentifier testIdentifier) {
		if (testIdentifier.isTest()) {
			runListener.testStarting(createReportEntry(testIdentifier));
		}
	}

	@Override
	public void executionSkipped(TestIdentifier testIdentifier, String reason) {
		runListener.testSkipped(
			ignored(getClassNameOrUniqueId(testIdentifier), testIdentifier.getDisplayName(), reason));
	}

	@Override
	public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
		if (testExecutionResult.getStatus() == ABORTED) {
			runListener.testAssumptionFailure(createReportEntry(testIdentifier, testExecutionResult.getThrowable()));
		}
		else if (testExecutionResult.getStatus() == FAILED) {
			runListener.testFailed(createReportEntry(testIdentifier, testExecutionResult.getThrowable()));
		}
		else if (testIdentifier.isTest()) {
			runListener.testSucceeded(createReportEntry(testIdentifier));
		}
	}

	private SimpleReportEntry createReportEntry(TestIdentifier testIdentifier) {
		return new SimpleReportEntry(getClassNameOrUniqueId(testIdentifier), testIdentifier.getDisplayName());
	}

	private SimpleReportEntry createReportEntry(TestIdentifier testIdentifier, Optional<Throwable> throwable) {
		Optional<JavaSource> javaSource = getJavaSource(testIdentifier);
		if (javaSource.isPresent() && javaSource.get().getJavaClass().isPresent()) {
			Class<?> sourceClass = javaSource.get().getJavaClass().get();
			Optional<String> sourceMethodName = javaSource.get().getJavaMethodName();
			StackTraceWriter stackTraceWriter = getStackTraceWriter(sourceClass, sourceMethodName, throwable);
			return new SimpleReportEntry(getClassNameOrUniqueId(testIdentifier), testIdentifier.getDisplayName(),
				stackTraceWriter, null);
		}
		return ignored(getClassNameOrUniqueId(testIdentifier), testIdentifier.getDisplayName(),
			throwable.map(Throwable::getMessage).orElse(null));
	}

	private StackTraceWriter getStackTraceWriter(Class<?> sourceClass, Optional<String> sourceMethodName,
			Optional<Throwable> throwable) {
		String className = sourceClass.getName();
		String methodName = sourceMethodName.orElse("");
		return new PojoStackTraceWriter(className, methodName, throwable.orElse(null));
	}

	private String getClassNameOrUniqueId(TestIdentifier testIdentifier) {
		return getClassName(testIdentifier).orElse(testIdentifier.getUniqueId());
	}

	private Optional<String> getClassName(TestIdentifier testIdentifier) {
		Optional<JavaSource> javaSource = getJavaSource(testIdentifier);
		if (javaSource.isPresent()) {
			if (javaSource.get().getJavaClass().isPresent()) {
				return javaSource.get().getJavaClass().map(Class::getName);
			}
		}
		return Optional.empty();
	}

	private Optional<JavaSource> getJavaSource(TestIdentifier testIdentifier) {
		return testIdentifier.getSource().filter(JavaSource.class::isInstance).map(JavaSource.class::cast);
	}
}
