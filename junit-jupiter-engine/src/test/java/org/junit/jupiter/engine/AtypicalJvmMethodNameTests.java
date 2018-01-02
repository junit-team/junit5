/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.engine.kotlin.ArbitraryNamingKotlinTestCase;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.test.event.ExecutionEventRecorder;

/**
 * Integration tests for JVM languages that allow special characters
 * in method names (e.g., Kotlin, Groovy, etc.) which are forbidden in
 * Java source code.
 *
 * @since 5.1
 */
class AtypicalJvmMethodNameTests extends AbstractJupiterTestEngineTests {

	@Test
	void kotlinTestWithMethodNameContainingSpecialCharacters() {
		ExecutionEventRecorder eventRecorder = executeTestsForClass(ArbitraryNamingKotlinTestCase.class);
		assertThat(eventRecorder.getTestFinishedCount()).isEqualTo(1);

		TestDescriptor testDescriptor = eventRecorder.getSuccessfulTestFinishedEvents().get(0).getTestDescriptor();
		assertAll(//
			() -> assertEquals(ArbitraryNamingKotlinTestCase.METHOD_NAME + "()", testDescriptor.getDisplayName()), //
			() -> assertEquals(ArbitraryNamingKotlinTestCase.METHOD_NAME + "()",
				testDescriptor.getLegacyReportingName()));
	}

}
