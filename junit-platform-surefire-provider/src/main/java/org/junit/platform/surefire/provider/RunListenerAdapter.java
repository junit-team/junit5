/*
 * Copyright 2015-2017 the original author or authors.
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

/**
 * @since 1.0
 */
final class RunListenerAdapter implements TestExecutionListener {

	private final RunListener runListener;
	private Optional<TestPlan> testPlan = Optional.empty();

	public RunListenerAdapter(RunListener runListener) {
		this.runListener = runListener;
	}

	@Override
	public void testPlanExecutionStarted(TestPlan testPlan) {
		this.testPlan = Optional.of(testPlan);
	}

	@Override
	public void testPlanExecutionFinished(TestPlan testPlan) {
		this.testPlan = Optional.empty();
	}

	@Override
	public void executionStarted(TestIdentifier testIdentifier) {
		if (testIdentifier.isTest()) {
			runListener.testStarting(createReportEntry(testIdentifier, Optional.empty()));
		}
	}

	@Override
	public void executionSkipped(TestIdentifier testIdentifier, String reason) {
		String source = sourceLegacyReportingName(testIdentifier);
		runListener.testSkipped(ignored(source, testIdentifier.getLegacyReportingName(), reason));
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
		Optional<String> className = getClassName(testIdentifier);

		Optional<StackTraceWriter> stackTraceWriter = throwable.flatMap(
			t -> className.map(name -> new PojoStackTraceWriter(name, getMethodName(testIdentifier).orElse(""), t)));

		String source = sourceLegacyReportingName(testIdentifier);
		return new SimpleReportEntry(source, testIdentifier.getLegacyReportingName(), stackTraceWriter.orElse(null),
			null);
	}

	private String sourceLegacyReportingName(TestIdentifier testIdentifier) {
		return testPlan.flatMap(plan -> plan.getParent(testIdentifier)).map(
			TestIdentifier::getLegacyReportingName).orElse("<unrooted>");
	}

	private Optional<String> getClassName(TestIdentifier testIdentifier) {
		TestSource testSource = testIdentifier.getSource().orElse(null);
		if (testSource instanceof ClassSource) {
			return Optional.of(((ClassSource) testSource).getJavaClass().getName());
		}
		if (testSource instanceof MethodSource) {
			return Optional.of(((MethodSource) testSource).getClassName());
		}
		return Optional.empty();
	}

	private Optional<String> getMethodName(TestIdentifier testIdentifier) {
		TestSource testSource = testIdentifier.getSource().orElse(null);
		if (testSource instanceof MethodSource) {
			return Optional.of(((MethodSource) testSource).getMethodName());
		}
		return Optional.empty();
	}

}
