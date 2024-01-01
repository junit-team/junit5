/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.testkit.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

import java.util.Optional;
import java.util.function.UnaryOperator;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.platform.engine.reporting.ReportEntry;

class EngineTestKitTests {

	private static final String KEY = EngineTestKitTests.class.getName();

	@BeforeEach
	void setSystemProperty() {
		System.setProperty(KEY, "from system property");
	}

	@AfterEach
	void resetSystemProperty() {
		System.clearProperty(KEY);
	}

	@Test
	void ignoresImplicitConfigurationParametersByDefault() {
		var value = executeExampleTestCaseAndCollectValue(builder -> builder);

		assertThat(value).isEmpty();
	}

	@ParameterizedTest
	@CsvSource({ "true, from system property", "false," })
	void usesImplicitConfigurationParametersWhenEnabled(boolean enabled, String expectedValue) {
		var value = executeExampleTestCaseAndCollectValue(
			builder -> builder.enableImplicitConfigurationParameters(enabled));

		assertThat(value).isEqualTo(Optional.ofNullable(expectedValue));
	}

	private Optional<String> executeExampleTestCaseAndCollectValue(UnaryOperator<EngineTestKit.Builder> configuration) {
		return configuration.apply(EngineTestKit.engine("junit-jupiter")) //
				.selectors(selectClass(ExampleTestCase.class)) //
				.execute() //
				.allEvents() //
				.reportingEntryPublished() //
				.map(event -> event.getPayload(ReportEntry.class).orElseThrow()) //
				.map(ReportEntry::getKeyValuePairs) //
				.map(entries -> entries.get(KEY)) //
				.findFirst();
	}

	static class ExampleTestCase {

		@RegisterExtension
		BeforeEachCallback callback = context -> context.getConfigurationParameter(KEY) //
				.ifPresent(value -> context.publishReportEntry(KEY, value));

		@Test
		void test() {
		}

	}

}
