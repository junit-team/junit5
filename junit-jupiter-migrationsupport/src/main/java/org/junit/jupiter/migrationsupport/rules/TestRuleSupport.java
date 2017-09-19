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

import static org.junit.platform.commons.util.AnnotationUtils.findPublicAnnotatedFields;
import static org.junit.platform.commons.util.AnnotationUtils.isAnnotated;
import static org.junit.platform.commons.util.ReflectionUtils.findMethods;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.junit.Rule;
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
class TestRuleSupport implements BeforeEachCallback, TestExecutionExceptionHandler, AfterEachCallback {

	private static final Consumer<List<Member>> NO_OP = members -> {
	};

	private final Class<? extends TestRule> ruleType;
	private final Function<TestRuleAnnotatedMember, AbstractTestRuleAdapter> adapterGenerator;

	TestRuleSupport(Function<TestRuleAnnotatedMember, AbstractTestRuleAdapter> adapterGenerator,
			Class<? extends TestRule> ruleType) {

		this.adapterGenerator = adapterGenerator;
		this.ruleType = ruleType;
	}

	private List<Member> findRuleAnnotatedMembers(Object testInstance) {
		List<Member> members = new ArrayList<>();
		members.addAll(findAnnotatedFields(testInstance));
		members.addAll(findAnnotatedMethods(testInstance));
		return members;
	}

	private List<Method> findAnnotatedMethods(Object testInstance) {
		Predicate<Method> isRuleMethod = method -> isAnnotated(method, Rule.class);
		Predicate<Method> hasCorrectReturnType = method -> method.getReturnType().isAssignableFrom(getRuleType());

		return findMethods(testInstance.getClass(), isRuleMethod.and(hasCorrectReturnType));
	}

	private List<Field> findAnnotatedFields(Object testInstance) {
		return findPublicAnnotatedFields(testInstance.getClass(), getRuleType(), Rule.class);
	}

	private Class<? extends TestRule> getRuleType() {
		return this.ruleType;
	}

	@Override
	public void beforeEach(ExtensionContext context) throws Exception {
		invokeAppropriateMethodOnRuleAnnotatedMembers(context, NO_OP, GenericBeforeAndAfterAdvice::before);
	}

	@Override
	public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
		int numRuleAnnotatedMembers = invokeAppropriateMethodOnRuleAnnotatedMembers(context, Collections::reverse,
			advice -> {
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
		invokeAppropriateMethodOnRuleAnnotatedMembers(context, Collections::reverse,
			GenericBeforeAndAfterAdvice::after);
	}

	/**
	 * @return the number of appropriate rule-annotated members that were discovered
	 */
	private int invokeAppropriateMethodOnRuleAnnotatedMembers(ExtensionContext context, Consumer<List<Member>> ordering,
			Consumer<GenericBeforeAndAfterAdvice> methodCaller) {

		Object testInstance = context.getRequiredTestInstance();
		List<Member> members = findRuleAnnotatedMembers(testInstance);
		ordering.accept(members);

		// @formatter:off
		members.stream()
				.map(member -> TestRuleAnnotatedMemberFactory.from(testInstance, member))
				.map(this.adapterGenerator)
				.forEach(methodCaller::accept);
		// @formatter:on

		return members.size();
	}

}
