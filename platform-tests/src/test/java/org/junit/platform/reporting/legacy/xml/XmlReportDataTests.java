/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.reporting.legacy.xml;

import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.platform.engine.TestExecutionResult.failed;
import static org.junit.platform.engine.TestExecutionResult.successful;

import java.time.Clock;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;
import org.junit.platform.fakes.TestDescriptorStub;
import org.junit.platform.launcher.TestPlan;

/**
 * @since 1.0
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
