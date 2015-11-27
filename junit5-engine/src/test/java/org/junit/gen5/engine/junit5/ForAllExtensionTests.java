/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.junit.Assert;
import org.junit.gen5.api.AfterAll;
import org.junit.gen5.api.Assertions;
import org.junit.gen5.api.BeforeAll;
import org.junit.gen5.api.ForAll;
import org.junit.gen5.api.Test;

/**
 * Core integration tests for the {@link JUnit5TestEngine}.
 *
 * @author Sam Brannen
 * @since 5.0
 */
public class ForAllExtensionTests extends AbstractJUnit5TestEngineTestCase {

	@org.junit.Test
	public void executeTestsForClass() {
		ForAllTestCase.countBeforeInvoked = 0;
		ForAllTestCase.countAfterInvoked = 0;

		TrackingTestExecutionListener listener = executeTestsForClass(ForAllTestCase.class, 3);

		Assert.assertEquals("# tests started", 2, listener.testStartedCount.get());
		Assert.assertEquals("# tests succeeded", 2, listener.testSucceededCount.get());

		Assert.assertEquals("# before all calls", 1, ForAllTestCase.countBeforeInvoked);
		Assert.assertEquals("# after all calls", 1, ForAllTestCase.countAfterInvoked);
	}

	private static class ForAllTestCase {

		static int countBeforeInvoked = 0;
		static int countAfterInvoked = 0;

		@ForAll
		static class MyForAll {

			String aVariable = "variable unchanged";

			@BeforeAll
			void myBeforeAll() {
				aVariable = "variable changed";
				countBeforeInvoked++;
			}

			@AfterAll
			void myAfterAll() {
				countAfterInvoked++;
			}
		}

		@Test
		void test1(MyForAll my) {
			Assertions.assertEquals("variable changed", my.aVariable);
		}

		@Test
		void test2() {

		}

	}

	@Test
	@Retention(RetentionPolicy.RUNTIME)
	@interface CustomTestAnnotation {
	}

}
