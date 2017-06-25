/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.migrationsupport.rules;

import java.lang.reflect.Member;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.junit.jupiter.api.extension.TestExtensionContext;
import org.junit.jupiter.migrationsupport.rules.adapter.AbstractTestRuleAdapter;
import org.junit.jupiter.migrationsupport.rules.adapter.GenericBeforeAndAfterAdvice;
import org.junit.jupiter.migrationsupport.rules.member.TestRuleAnnotatedMember;
import org.junit.jupiter.migrationsupport.rules.member.TestRuleAnnotatedMemberFactory;
import org.junit.platform.commons.util.ExceptionUtils;
import org.junit.rules.TestRule;

/**
 * @since 5.0
 */
abstract class AbstractTestRuleSupport<T extends Member>
		implements BeforeEachCallback, TestExecutionExceptionHandler, AfterEachCallback {

	private final Class<? extends TestRule> ruleType;
	private final Function<TestRuleAnnotatedMember, AbstractTestRuleAdapter> adapterGenerator;

	AbstractTestRuleSupport(Function<TestRuleAnnotatedMember, AbstractTestRuleAdapter> adapterGenerator,
			Class<? extends TestRule> ruleType) {
		this.adapterGenerator = adapterGenerator;
		this.ruleType = ruleType;
	}

	protected abstract List<T> findRuleAnnotatedMembers(Object testInstance);

	protected Class<? extends TestRule> getRuleType() {
		return this.ruleType;
	}

	@Override
	public void beforeEach(TestExtensionContext context) throws Exception {
		invokeAppropriateMethodOnRuleAnnotatedMembers(context, GenericBeforeAndAfterAdvice::before);
	}

	@Override
	public void handleTestExecutionException(TestExtensionContext context, Throwable throwable) throws Throwable {
		invokeAppropriateMethodOnRuleAnnotatedMembers(context, advice -> {
			try {
				advice.handleTestExecutionException(throwable);
			}
			catch (Throwable t) {
				throw ExceptionUtils.throwAsUncheckedException(t);
			}
		});
	}

	@Override
	public void afterEach(TestExtensionContext context) throws Exception {
		invokeAppropriateMethodOnRuleAnnotatedMembers(context, GenericBeforeAndAfterAdvice::after);
	}

	private void invokeAppropriateMethodOnRuleAnnotatedMembers(TestExtensionContext context,
			Consumer<GenericBeforeAndAfterAdvice> methodCaller) {

		Object testInstance = context.getTestInstance().get();
		List<T> members = findRuleAnnotatedMembers(testInstance);

		// @formatter:off
		members.stream()
				.map(member -> TestRuleAnnotatedMemberFactory.from(testInstance, member))
				.map(this.adapterGenerator)
				.forEach(methodCaller::accept);
		// @formatter:on
	}

}
