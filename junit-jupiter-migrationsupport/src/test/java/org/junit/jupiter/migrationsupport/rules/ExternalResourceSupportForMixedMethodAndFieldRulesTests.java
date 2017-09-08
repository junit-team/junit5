/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.migrationsupport.rules;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.migrationsupport.rules.FailAfterAllHelper.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Rule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.rules.ExternalResource;

@ExtendWith(ExternalResourceSupport.class)
public class ExternalResourceSupportForMixedMethodAndFieldRulesTests {

	private static List<String> beforeEvents = new ArrayList<>();
	private static List<String> afterEvents = new ArrayList<>();

	@BeforeAll
	static void clear() {
		beforeEvents.clear();
		afterEvents.clear();
	}

	@Rule
	public ExternalResource fieldRule = new ExternalResource() {
		@Override
		protected void before() throws Throwable {
			beforeEvents.add("fieldRule");
		}

		@Override
		protected void after() {
			afterEvents.add("fieldRule");
		}
	};

	@Rule
	ExternalResource getResource2() {
		return new ExternalResource() {
			@Override
			protected void before() throws Throwable {
				beforeEvents.add("methodRule");
			}

			@Override
			protected void after() {
				afterEvents.add("methodRule");
			}
		};
	}

	@Test
	void beforeMethodsOfBothRulesWereExecuted() {
		assertEquals(asList("fieldRule", "methodRule"), beforeEvents);
	}

	@AfterAll
	static void afterMethodsOfBothRulesWereExecuted() {
		if (!asList("methodRule", "fieldRule").equals(afterEvents))
			fail();
	}

}
