/*
 * Copyright 2015-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.junit.platform.surefire.provider;

import static org.apache.maven.surefire.report.SimpleReportEntry.ignored;
import static org.junit.platform.engine.TestExecutionResult.Status.ABORTED;
import static org.junit.platform.engine.TestExecutionResult.Status.FAILED;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.maven.surefire.report.PojoStackTraceWriter;
import org.apache.maven.surefire.report.RunListener;
import org.apache.maven.surefire.report.SimpleReportEntry;
import org.apache.maven.surefire.report.StackTraceWriter;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.listeners.LegacyReportingUtils;

/**
 * @since 1.0
 */
final class RunListenerAdapter implements TestExecutionListener {

	private final RunListener runListener;
	private TestPlan testPlan;
	private Set<TestIdentifier> testSetNodes = ConcurrentHashMap.newKeySet();

	RunListenerAdapter(RunListener runListener) {
		this.runListener = runListener;
	}

	@Override
	public void testPlanExecutionStarted(TestPlan testPlan) {
		updateTestPlan(testPlan);
	}

	@Override
	public void testPlanExecutionFinished(TestPlan testPlan) {
		updateTestPlan(null);
	}

	@Override
	public void executionStarted(TestIdentifier testIdentifier) {
		if (testIdentifier.isContainer()
				&& testIdentifier.getSource().filter(ClassSource.class::isInstance).isPresent()) {
			startTestSetIfPossible(testIdentifier);
		}
		if (testIdentifier.isTest()) {
			ensureTestSetStarted(testIdentifier);
			runListener.testStarting(createReportEntry(testIdentifier));
		}
	}

	@Override
	public void executionSkipped(TestIdentifier testIdentifier, String reason) {
		ensureTestSetStarted(testIdentifier);
		String source = getLegacyReportingClassName(testIdentifier);
		runListener.testSkipped(ignored(source, getLegacyReportingName(testIdentifier), reason));
		completeTestSetIfNecessary(testIdentifier);
	}

	@Override
	public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
		if (testExecutionResult.getStatus() == ABORTED) {
			runListener.testAssumptionFailure(createReportEntry(testIdentifier, testExecutionResult));
		}
		else if (testExecutionResult.getStatus() == FAILED) {
			reportFailedTest(testIdentifier, testExecutionResult);
		}
		else if (testIdentifier.isTest()) {
			runListener.testSucceeded(createReportEntry(testIdentifier));
		}
		completeTestSetIfNecessary(testIdentifier);
	}

	private void updateTestPlan(TestPlan testPlan) {
		this.testPlan = testPlan;
		testSetNodes.clear();
	}

	private void ensureTestSetStarted(TestIdentifier testIdentifier) {
		if (isTestSetStarted(testIdentifier)) {
			return;
		}
		if (testIdentifier.isTest()) {
			startTestSet(testPlan.getParent(testIdentifier).orElse(testIdentifier));
		}
		else {
			startTestSet(testIdentifier);
		}
	}

	private boolean isTestSetStarted(TestIdentifier testIdentifier) {
		return testSetNodes.contains(testIdentifier)
				|| testPlan.getParent(testIdentifier).map(this::isTestSetStarted).orElse(false);
	}

	private void startTestSetIfPossible(TestIdentifier testIdentifier) {
		if (!isTestSetStarted(testIdentifier)) {
			startTestSet(testIdentifier);
		}
	}

	private void completeTestSetIfNecessary(TestIdentifier testIdentifier) {
		if (testSetNodes.contains(testIdentifier)) {
			completeTestSet(testIdentifier);
		}
	}

	private void startTestSet(TestIdentifier testIdentifier) {
		runListener.testSetStarting(createTestSetReportEntry(testIdentifier));
		testSetNodes.add(testIdentifier);
	}

	private void completeTestSet(TestIdentifier testIdentifier) {
		runListener.testSetCompleted(createTestSetReportEntry(testIdentifier));
		testSetNodes.remove(testIdentifier);
	}

	private void reportFailedTest(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
		SimpleReportEntry reportEntry = createReportEntry(testIdentifier, testExecutionResult);
		if (testExecutionResult.getThrowable().filter(AssertionError.class::isInstance).isPresent()) {
			runListener.testFailed(reportEntry);
		}
		else {
			runListener.testError(reportEntry);
		}
	}

	@SuppressWarnings("deprecation")
	private SimpleReportEntry createTestSetReportEntry(TestIdentifier testIdentifier) {
		return new SimpleReportEntry(JUnitPlatformProvider.class.getName(), testIdentifier.getLegacyReportingName());
	}

	private SimpleReportEntry createReportEntry(TestIdentifier testIdentifier) {
		return createReportEntry(testIdentifier, (StackTraceWriter) null);
	}

	private SimpleReportEntry createReportEntry(TestIdentifier testIdentifier,
			TestExecutionResult testExecutionResult) {
		return createReportEntry(testIdentifier, getStackTraceWriter(testIdentifier, testExecutionResult));
	}

	private SimpleReportEntry createReportEntry(TestIdentifier testIdentifier, StackTraceWriter stackTraceWriter) {
		String source = getLegacyReportingClassName(testIdentifier);
		String name = getLegacyReportingName(testIdentifier);

		return SimpleReportEntry.withException(source, name, stackTraceWriter);
	}

	private String getLegacyReportingName(TestIdentifier testIdentifier) {
		// Surefire cuts off the name at the first '(' character. Thus, we have to pick a different
		// character to represent parentheses. "()" are removed entirely to maximize compatibility with
		// existing reporting tools because in the old days test methods used to not have parameters.
		return testIdentifier.getLegacyReportingName().replace("()", "").replace('(', '{').replace(')', '}');
	}

	private String getLegacyReportingClassName(TestIdentifier testIdentifier) {
		return LegacyReportingUtils.getClassName(testPlan, testIdentifier);
	}

	private StackTraceWriter getStackTraceWriter(TestIdentifier testIdentifier,
			TestExecutionResult testExecutionResult) {
		Optional<Throwable> throwable = testExecutionResult.getThrowable();
		if (testExecutionResult.getStatus() == FAILED) {
			// Failed tests must have a StackTraceWriter, otherwise Surefire will fail
			return getStackTraceWriter(testIdentifier, throwable.orElse(null));
		}
		return throwable.map(t -> getStackTraceWriter(testIdentifier, t)).orElse(null);
	}

	private StackTraceWriter getStackTraceWriter(TestIdentifier testIdentifier, Throwable throwable) {
		String className = getClassName(testIdentifier);
		String methodName = getMethodName(testIdentifier).orElse("");
		return new PojoStackTraceWriter(className, methodName, throwable);
	}

	private String getClassName(TestIdentifier testIdentifier) {
		TestSource testSource = testIdentifier.getSource().orElse(null);
		if (testSource instanceof ClassSource) {
			return ((ClassSource) testSource).getJavaClass().getName();
		}
		if (testSource instanceof MethodSource) {
			return ((MethodSource) testSource).getClassName();
		}
		// @formatter:off
		return testPlan.getParent(testIdentifier)
				.map(this::getClassName)
				.orElse("");
		// @formatter:on
	}

	private Optional<String> getMethodName(TestIdentifier testIdentifier) {
		TestSource testSource = testIdentifier.getSource().orElse(null);
		if (testSource instanceof MethodSource) {
			return Optional.of(((MethodSource) testSource).getMethodName());
		}
		return Optional.empty();
	}

}
