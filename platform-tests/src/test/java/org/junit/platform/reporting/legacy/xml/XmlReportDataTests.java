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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.platform.engine.TestExecutionResult.failed;
import static org.junit.platform.engine.TestExecutionResult.successful;

import java.time.Clock;
import java.util.Set;

import org.junit.jupiter.api.Test;
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
		var engineDescriptor = new EngineDescriptor(UniqueId.forEngine("engine"), "Engine");
		engineDescriptor.addChild(new TestDescriptorStub(UniqueId.root("child", "test"), "test"));
		var testPlan = TestPlan.from(Set.of(engineDescriptor));

		var reportData = new XmlReportData(testPlan, Clock.systemDefaultZone());
		var result = reportData.getResult(testPlan.getTestIdentifier("[child:test]"));

		assertThat(result).isEmpty();
	}

	@Test
	void resultOfTestIdentifierWithoutReportedEventsIsFailureOfAncestor() {
		var engineDescriptor = new EngineDescriptor(UniqueId.forEngine("engine"), "Engine");
		engineDescriptor.addChild(new TestDescriptorStub(UniqueId.root("child", "test"), "test"));
		var testPlan = TestPlan.from(Set.of(engineDescriptor));

		var reportData = new XmlReportData(testPlan, Clock.systemDefaultZone());
		var failureOfAncestor = failed(new RuntimeException("failed!"));
		reportData.markFinished(testPlan.getTestIdentifier("[engine:engine]"), failureOfAncestor);

		var result = reportData.getResult(testPlan.getTestIdentifier("[child:test]"));

		assertThat(result).contains(failureOfAncestor);
	}

	@Test
	void resultOfTestIdentifierWithoutReportedEventsIsEmptyWhenAncestorWasSuccessful() {
		var engineDescriptor = new EngineDescriptor(UniqueId.forEngine("engine"), "Engine");
		engineDescriptor.addChild(new TestDescriptorStub(UniqueId.root("child", "test"), "test"));
		var testPlan = TestPlan.from(Set.of(engineDescriptor));

		var reportData = new XmlReportData(testPlan, Clock.systemDefaultZone());
		reportData.markFinished(testPlan.getTestIdentifier("[engine:engine]"), successful());

		var result = reportData.getResult(testPlan.getTestIdentifier("[child:test]"));

		assertThat(result).isEmpty();
	}
}
