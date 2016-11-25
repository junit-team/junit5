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

import static org.junit.platform.commons.meta.API.Usage.Experimental;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.TestExtensionContext;
import org.junit.jupiter.migrationsupport.rules.adapter.AbstractTestRuleAdapter;
import org.junit.jupiter.migrationsupport.rules.member.RuleAnnotatedField;
import org.junit.jupiter.migrationsupport.rules.member.RuleAnnotatedMember;
import org.junit.platform.commons.meta.API;
import org.junit.rules.TestRule;

@API(Experimental)
class TestRuleFieldSupport extends AbstractTestRuleSupport {

	TestRuleFieldSupport(Function<RuleAnnotatedMember, AbstractTestRuleAdapter> adapterGenerator,
			Class<? extends TestRule> ruleType) {
		super(adapterGenerator, ruleType);
	}

	@Override
	protected RuleAnnotatedMember createRuleAnnotatedMember(TestExtensionContext context, Member member) {
		return new RuleAnnotatedField(context.getTestInstance(), (Field) member);
	}

	@Override
	protected List<Member> findRuleAnnotatedMembers(Object testInstance) {
		return findAnnotatedFields(testInstance, super.getRuleType(), super.getAnnotationType()).collect(
			Collectors.toList());
	}

	private static Stream<Field> findAnnotatedFields(Object instance, Class<?> fieldType,
			Class<? extends Annotation> annotationType) {
		Field[] fields = instance.getClass().getFields();

		// @formatter:off
        return Arrays.stream(fields)
                .filter(field -> fieldType.isAssignableFrom(field.getType()))
                .filter(field -> field.isAnnotationPresent(annotationType));
		// @formatter:on
	}

}
