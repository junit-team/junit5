/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.migrationsupport.rules;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.junit.platform.commons.meta.API.Usage.Experimental;

import java.util.function.Function;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.junit.jupiter.api.extension.TestExtensionContext;
import org.junit.jupiter.migrationsupport.rules.adapter.AbstractTestRuleAdapter;
import org.junit.jupiter.migrationsupport.rules.adapter.ExpectedExceptionAdapter;
import org.junit.jupiter.migrationsupport.rules.member.RuleAnnotatedMember;
import org.junit.platform.commons.meta.API;
import org.junit.rules.ExpectedException;

/**
 * This {@code Extension} provides native support for the
 * {@code ExpectedException} rule from JUnit 4.
 *
 * <p>By using this class-level extension on a test class,
 * {@code ExpectedException} can continued to be used.
 *
 * <p>However, you should rather switch to
 * {@link org.junit.jupiter.api.Assertions#assertThrows} for new code.
 *
 * @since 5.0
 * @see org.junit.jupiter.api.Assertions#assertThrows
 * @see ExpectedException
 * @see org.junit.rules.TestRule
 * @see org.junit.Rule
 */
@API(Experimental)
public class ExpectedExceptionSupport implements AfterEachCallback, TestExecutionExceptionHandler {

	private static final String EXCEPTION_WAS_HANDLED = "exceptionWasHandled";

	private final Function<RuleAnnotatedMember, AbstractTestRuleAdapter> adapterGenerator = ExpectedExceptionAdapter::new;

	private final AbstractTestRuleSupport fieldSupport = new TestRuleFieldSupport(this.adapterGenerator,
		ExpectedException.class);
	private final AbstractTestRuleSupport methodSupport = new TestRuleMethodSupport(this.adapterGenerator,
		ExpectedException.class);

	@Override
	public void handleTestExecutionException(TestExtensionContext context, Throwable throwable) throws Throwable {
		getStore(context).put(EXCEPTION_WAS_HANDLED, TRUE);
		this.methodSupport.handleTestExecutionException(context, throwable);
		this.fieldSupport.handleTestExecutionException(context, throwable);
	}

	@Override
	public void afterEach(TestExtensionContext context) throws Exception {
		boolean exceptionWasHandled = getStore(context).getOrComputeIfAbsent(EXCEPTION_WAS_HANDLED, key -> FALSE,
			Boolean.class);
		if (!exceptionWasHandled) {
			this.methodSupport.afterEach(context);
			this.fieldSupport.afterEach(context);
		}
	}

	private Store getStore(TestExtensionContext context) {
		return context.getStore(Namespace.create(ExpectedExceptionSupport.class, context.getUniqueId()));
	}
}
