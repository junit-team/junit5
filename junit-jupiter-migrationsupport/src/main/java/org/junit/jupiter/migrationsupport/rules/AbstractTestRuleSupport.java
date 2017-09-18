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

import java.lang.reflect.Member;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
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
	public void beforeEach(ExtensionContext context) throws Exception {
		invokeAppropriateMethodOnRuleAnnotatedMembers(context, GenericBeforeAndAfterAdvice::before);
	}

	@Override
	public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
		int numRuleAnnotatedMembers = invokeAppropriateMethodOnRuleAnnotatedMembers(context, advice -> {
			try {
				advice.handleTestExecutionException(throwable);
			}
			catch (Throwable t) {
				throw ExceptionUtils.throwAsUncheckedException(t);
			}
		});

		// If no appropriate @Rule annotated members were discovered, we then
		// have to rethrow the exception in order not to silently swallow it.
		// Fixes bug: https://github.com/junit-team/junit5/issues/1069
		if (numRuleAnnotatedMembers == 0) {
			throw throwable;
		}
	}

	@Override
	public void afterEach(ExtensionContext context) throws Exception {
		invokeAppropriateMethodOnRuleAnnotatedMembers(context, GenericBeforeAndAfterAdvice::after);
	}

	/**
	 * @return the number of appropriate rule-annotated members that were discovered
	 */
	private int invokeAppropriateMethodOnRuleAnnotatedMembers(ExtensionContext context,
			Consumer<GenericBeforeAndAfterAdvice> methodCaller) {

		Object testInstance = context.getRequiredTestInstance();
		List<T> members = findRuleAnnotatedMembers(testInstance);

		// @formatter:off
		members.stream()
				.map(member -> TestRuleAnnotatedMemberFactory.from(testInstance, member))
				.map(this.adapterGenerator)
				.forEach(methodCaller::accept);
		// @formatter:on

		return members.size();
	}

}
