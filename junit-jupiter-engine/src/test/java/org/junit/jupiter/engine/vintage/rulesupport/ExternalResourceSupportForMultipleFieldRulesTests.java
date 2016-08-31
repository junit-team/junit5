/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine.vintage.rulesupport;

import org.junit.Rule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.rules.ExternalResource;

@ExtendWith(ExternalResourceSupport.class)
public class ExternalResourceSupportForMultipleFieldRulesTests {

	private static boolean beforeOfRule1WasExecuted = false;
	private static boolean beforeOfRule2WasExecuted = false;

	private static boolean afterOfRule1WasExecuted = false;
	private static boolean afterOfRule2WasExecuted = false;

	@Rule
	public ExternalResource resource1 = new ExternalResource() {
		@Override
		protected void before() throws Throwable {
			beforeOfRule1WasExecuted = true;
		}

		@Override
		protected void after() {
			afterOfRule1WasExecuted = true;
		}
	};

	@Rule
	public ExternalResource resource2 = new ExternalResource() {
		@Override
		protected void before() throws Throwable {
			beforeOfRule2WasExecuted = true;
		}

		@Override
		protected void after() {
			afterOfRule2WasExecuted = true;
		}
	};

	@Test
	void beforeMethodsOfBothRulesWereExecuted() {
		assert beforeOfRule1WasExecuted;
		assert beforeOfRule2WasExecuted;
	}

	@AfterAll
	static void afterMethodsOfBothRulesWereExecuted() {
		assert afterOfRule1WasExecuted;
		assert afterOfRule2WasExecuted;
	}

}
