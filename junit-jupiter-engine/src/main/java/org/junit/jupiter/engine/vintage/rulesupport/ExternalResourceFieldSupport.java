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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.TestExtensionContext;

class ExternalResourceFieldSupport extends AbstractExternalResourceSupport {

	@Override
	protected RuleAnnotatedMember createRuleAnnotatedMember(TestExtensionContext context, Member member) {
		return new RuleAnnotatedField(context, (Field) member);
	}

	@Override
	protected List<Member> findRuleAnnotatedMembers(Object testInstance) {
		return findAnnotatedFields(testInstance, super.getRuleType(), super.getAnnotationType()).collect(
			Collectors.toList());
	}

	// TODO: decide whether this should be promoted to AnnotationUtils
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
