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

import static org.junit.platform.commons.util.AnnotationUtils.isAnnotated;
import static org.junit.platform.commons.util.ReflectionUtils.findMethods;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import org.junit.Rule;
import org.junit.jupiter.migrationsupport.rules.adapter.AbstractTestRuleAdapter;
import org.junit.jupiter.migrationsupport.rules.member.TestRuleAnnotatedMember;
import org.junit.rules.TestRule;

/**
 * @since 5.0
 */
class TestRuleMethodSupport extends AbstractTestRuleSupport<Method> {

	TestRuleMethodSupport(Function<TestRuleAnnotatedMember, AbstractTestRuleAdapter> adapterGenerator,
			Class<? extends TestRule> ruleType) {
		super(adapterGenerator, ruleType);
	}

	@Override
	protected List<Method> findRuleAnnotatedMembers(Object testInstance) {
		Predicate<Method> isRuleMethod = method -> isAnnotated(method, Rule.class);
		Predicate<Method> hasCorrectReturnType = method -> method.getReturnType().isAssignableFrom(getRuleType());

		return findMethods(testInstance.getClass(), isRuleMethod.and(hasCorrectReturnType));
	}

}
