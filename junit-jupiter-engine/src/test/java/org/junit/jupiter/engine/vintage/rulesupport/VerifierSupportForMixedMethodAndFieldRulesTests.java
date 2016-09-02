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
import org.junit.rules.Verifier;

@ExtendWith(VerifierSupport.class)
public class VerifierSupportForMixedMethodAndFieldRulesTests {

	private static boolean afterOfRule1WasExecuted = false;
	private static boolean afterOfRule2WasExecuted = false;

	@Rule
	public Verifier verifier1 = new Verifier() {

		@Override
		protected void verify() throws Throwable {
			afterOfRule1WasExecuted = true;
		}
	};

	private Verifier verifier2 = new Verifier() {

		@Override
		protected void verify() throws Throwable {
			afterOfRule2WasExecuted = true;
		}
	};

	@Rule
	public Verifier getVerifier2() {
		return verifier2;
	}

	@Test
	void testNothing() {
		//needed to start the test process at all
	}

	@AfterAll
	static void afterMethodsOfBothRulesWereExecuted() {
		if (!afterOfRule1WasExecuted)
			fail();
		if (!afterOfRule2WasExecuted)
			fail();
	}

	static void fail() {
		//hack: use this blacklisted exception to fail the build, all others would be swallowed...
		throw new OutOfMemoryError("a postcondition was violated");
	}

}
