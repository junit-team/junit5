/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.console.tasks;

import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.gen5.engine.TestExecutionResult.failed;
import static org.junit.gen5.engine.TestExecutionResult.successful;

import java.time.Clock;
import java.util.Optional;

import org.junit.gen5.api.Test;
import org.junit.gen5.engine.TestExecutionResult;
import org.junit.gen5.engine.UniqueId;
import org.junit.gen5.engine.support.descriptor.EngineDescriptor;
import org.junit.gen5.engine.test.TestDescriptorStub;
import org.junit.gen5.launcher.TestPlan;

/**
 * @since 5.0
 */
class XmlReportDataTests {

	@Test
	void resultOfTestIdentifierWithoutAnyReportedEventsIsEmpty() {
		EngineDescriptor engineDescriptor = new EngineDescriptor(UniqueId.forEngine("engine"), "Engine");
		engineDescriptor.addChild(new TestDescriptorStub(UniqueId.root("child", "test"), "test"));
		TestPlan testPlan = TestPlan.from(singleton(engineDescriptor));

		XmlReportData reportData = new XmlReportData(testPlan, Clock.systemDefaultZone());
		Optional<TestExecutionResult> result = reportData.getResult(testPlan.getTestIdentifier("[child:test]"));

		assertThat(result).isEmpty();
	}

	@Test
	void resultOfTestIdentifierWithoutReportedEventsIsFailureOfAncestor() {
		EngineDescriptor engineDescriptor = new EngineDescriptor(UniqueId.forEngine("engine"), "Engine");
		engineDescriptor.addChild(new TestDescriptorStub(UniqueId.root("child", "test"), "test"));
		TestPlan testPlan = TestPlan.from(singleton(engineDescriptor));

		XmlReportData reportData = new XmlReportData(testPlan, Clock.systemDefaultZone());
		TestExecutionResult failureOfAncestor = failed(new RuntimeException("failed!"));
		reportData.markFinished(testPlan.getTestIdentifier("[engine:engine]"), failureOfAncestor);

		Optional<TestExecutionResult> result = reportData.getResult(testPlan.getTestIdentifier("[child:test]"));

		assertThat(result).contains(failureOfAncestor);
	}

	@Test
	void resultOfTestIdentifierWithoutReportedEventsIsEmptyWhenAncestorWasSuccessful() {
		EngineDescriptor engineDescriptor = new EngineDescriptor(UniqueId.forEngine("engine"), "Engine");
		engineDescriptor.addChild(new TestDescriptorStub(UniqueId.root("child", "test"), "test"));
		TestPlan testPlan = TestPlan.from(singleton(engineDescriptor));

		XmlReportData reportData = new XmlReportData(testPlan, Clock.systemDefaultZone());
		reportData.markFinished(testPlan.getTestIdentifier("[engine:engine]"), successful());

		Optional<TestExecutionResult> result = reportData.getResult(testPlan.getTestIdentifier("[child:test]"));

		assertThat(result).isEmpty();
	}
}
