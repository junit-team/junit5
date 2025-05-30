/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.migrationsupport.rules.adapter;

import static java.lang.Boolean.TRUE;
import static org.apiguardian.api.API.Status.DEPRECATED;

import org.apiguardian.api.API;
import org.junit.jupiter.migrationsupport.rules.member.TestRuleAnnotatedMember;
import org.junit.rules.ExpectedException;

/**
 * @since 5.0
 */
@SuppressWarnings("removal")
@API(status = DEPRECATED, since = "6.0")
@Deprecated(since = "6.0", forRemoval = true)
public class ExpectedExceptionAdapter extends AbstractTestRuleAdapter {

	public ExpectedExceptionAdapter(TestRuleAnnotatedMember annotatedMember) {
		super(annotatedMember, ExpectedException.class);
	}

	@Override
	public void handleTestExecutionException(Throwable cause) throws Throwable {
		executeMethod("handleException", new Class<?>[] { Throwable.class }, cause);
	}

	@Override
	public void after() {
		if (TRUE.equals(executeMethod("isAnyExceptionExpected"))) {
			executeMethod("failDueToMissingException");
		}
	}
}
