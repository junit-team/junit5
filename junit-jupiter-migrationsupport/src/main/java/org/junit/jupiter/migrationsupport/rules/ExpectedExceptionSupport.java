/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.migrationsupport.rules;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.apiguardian.api.API.Status.STABLE;

import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.junit.jupiter.migrationsupport.rules.adapter.ExpectedExceptionAdapter;
import org.junit.rules.ExpectedException;

/**
 * This {@code Extension} provides native support for the
 * {@link ExpectedException} rule from JUnit 4.
 *
 * <p>By using this class-level extension on a test class,
 * {@code ExpectedException} can continue to be used.
 *
 * <p>However, you should rather switch to
 * {@link org.junit.jupiter.api.Assertions#assertThrows} for new code.
 *
 * @since 5.0
 * @see org.junit.jupiter.api.Assertions#assertThrows
 * @see org.junit.rules.ExpectedException
 * @see org.junit.rules.TestRule
 * @see org.junit.Rule
 */
@API(status = STABLE, since = "5.7")
public class ExpectedExceptionSupport implements AfterEachCallback, TestExecutionExceptionHandler {

	private static final String EXCEPTION_WAS_HANDLED = "exceptionWasHandled";

	private final TestRuleSupport support = new TestRuleSupport(ExpectedExceptionAdapter::new, ExpectedException.class);

	public ExpectedExceptionSupport() {
	}

	@Override
	public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
		getStore(context).put(EXCEPTION_WAS_HANDLED, TRUE);
		this.support.handleTestExecutionException(context, throwable);
	}

	@Override
	public void afterEach(ExtensionContext context) throws Exception {
		boolean handled = getStore(context).getOrComputeIfAbsent(EXCEPTION_WAS_HANDLED, key -> FALSE, Boolean.class);
		if (!handled) {
			this.support.afterEach(context);
		}
	}

	private Store getStore(ExtensionContext context) {
		return context.getStore(Namespace.create(getClass(), context.getUniqueId()));
	}

}
