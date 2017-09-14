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

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Function;

import org.junit.Rule;
import org.junit.jupiter.migrationsupport.rules.adapter.AbstractTestRuleAdapter;
import org.junit.jupiter.migrationsupport.rules.member.TestRuleAnnotatedMember;
import org.junit.rules.TestRule;

/**
 * @since 5.0
 */
class TestRuleFieldSupport extends AbstractTestRuleSupport<Field> {

	TestRuleFieldSupport(Function<TestRuleAnnotatedMember, AbstractTestRuleAdapter> adapterGenerator,
			Class<? extends TestRule> ruleType) {
		super(adapterGenerator, ruleType);
	}

	@Override
	protected List<Field> findRuleAnnotatedMembers(Object testInstance) {
		return findPublicAnnotatedFields(testInstance.getClass(), getRuleType(), Rule.class);
	}

}
