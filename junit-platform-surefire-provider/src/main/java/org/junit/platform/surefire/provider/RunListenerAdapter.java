/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.surefire.provider;

import static org.apache.maven.surefire.report.SimpleReportEntry.ignored;
import static org.junit.platform.engine.TestExecutionResult.Status.ABORTED;
import static org.junit.platform.engine.TestExecutionResult.Status.FAILED;

import java.util.Optional;

import org.apache.maven.surefire.report.PojoStackTraceWriter;
import org.apache.maven.surefire.report.RunListener;
import org.apache.maven.surefire.report.SimpleReportEntry;
import org.apache.maven.surefire.report.StackTraceWriter;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.support.descriptor.JavaClassSource;
import org.junit.platform.engine.support.descriptor.JavaMethodSource;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;

/**
 * @since 1.0
 */
final class RunListenerAdapter implements TestExecutionListener {

	private final RunListener runListener;

	public RunListenerAdapter(RunListener runListener) {
		this.runListener = runListener;
	}

	@Override
	public void executionStarted(TestIdentifier testIdentifier) {
		if (testIdentifier.isTest()) {
			runListener.testStarting(createReportEntry(testIdentifier, Optional.empty()));
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
			runListener.testSucceeded(createReportEntry(testIdentifier, Optional.empty()));
		}
	}

	private SimpleReportEntry createReportEntry(TestIdentifier testIdentifier, Optional<Throwable> throwable) {
		TestSource testSource = testIdentifier.getSource().orElse(null);
		if (testSource instanceof JavaClassSource) {
			JavaClassSource javaClassSource = (JavaClassSource) testSource;
			String className = javaClassSource.getJavaClass().getName();
			StackTraceWriter stackTraceWriter = new PojoStackTraceWriter(className, "", throwable.orElse(null));
			return new SimpleReportEntry(className, testIdentifier.getDisplayName(), stackTraceWriter, null);
		}
		else if (testSource instanceof JavaMethodSource) {
			JavaMethodSource javaMethodSource = (JavaMethodSource) testSource;
			String className = javaMethodSource.getClassName();
			String methodName = javaMethodSource.getMethodName();
			StackTraceWriter stackTraceWriter = new PojoStackTraceWriter(className, methodName, throwable.orElse(null));
			return new SimpleReportEntry(className, testIdentifier.getDisplayName(), stackTraceWriter, null);
		}
		else {
			return ignored(testIdentifier.getUniqueId(), testIdentifier.getDisplayName(),
				throwable.map(Throwable::getMessage).orElse(null));
		}
	}

	private String getClassNameOrUniqueId(TestIdentifier testIdentifier) {
		TestSource testSource = testIdentifier.getSource().orElse(null);
		if (testSource instanceof JavaClassSource) {
			JavaClassSource javaClassSource = (JavaClassSource) testSource;
			return javaClassSource.getJavaClass().getName();
		}
		else if (testSource instanceof JavaMethodSource) {
			JavaMethodSource javaMethodSource = (JavaMethodSource) testSource;
			return javaMethodSource.getClassName();
		}
		else {
			return testIdentifier.getUniqueId();
		}
	}

}
