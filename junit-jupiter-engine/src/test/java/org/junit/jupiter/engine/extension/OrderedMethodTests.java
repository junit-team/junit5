/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.extension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.MethodOrdering;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.engine.AbstractJupiterTestEngineTests;
import org.junit.jupiter.engine.JupiterTestEngine;
import org.junit.platform.testkit.ExecutionResults;

/**
 * Integration tests that verify support for custom test method execution order
 * in the {@link JupiterTestEngine}.
 *
 * @since 5.4
 */
class OrderedMethodTests extends AbstractJupiterTestEngineTests {

	private static final Set<String> callSequence = new LinkedHashSet<>();

	@BeforeEach
	void clearCallSequence() {
		callSequence.clear();
	}

	@Test
	void orderAnnotation() {
		ExecutionResults executionResults = executeTestsForClass(OrderAnnotationTestCase.class);

		assertEquals(6, executionResults.getTestsStartedCount(), "# tests started");
		assertEquals(6, executionResults.getTestsSuccessfulCount(), "# tests succeeded");
		assertThat(callSequence).containsExactly("test1", "test2", "test3", "test4", "test5", "test6");
	}

	// -------------------------------------------------------------------------

	@MethodOrdering(OrderAnnotation.class)
	static class OrderAnnotationTestCase {

		@BeforeEach
		void trackInvocations(TestInfo testInfo) {
			callSequence.add(testInfo.getDisplayName());
		}

		@Test
		@DisplayName("test6")
		// @Order(6)
		void defaultOrderValue() {
		}

		@Test
		@DisplayName("test3")
		@Order(3)
		void $() {
		}

		@Test
		@DisplayName("test5")
		@Order(5)
		void AAA() {
		}

		@Test
		@DisplayName("test4")
		@Order(4)
		void aaa() {
		}

		@Test
		@DisplayName("test1")
		@Order(1)
		void zzz() {
		}

		@Test
		@DisplayName("test2")
		@Order(2)
		void ___() {
		}
	}

}
