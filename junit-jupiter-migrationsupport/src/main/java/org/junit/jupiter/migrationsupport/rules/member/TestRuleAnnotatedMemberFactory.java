/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.migrationsupport.rules.member;

import static org.junit.platform.commons.meta.API.Usage.Internal;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import org.junit.platform.commons.meta.API;
import org.junit.platform.commons.util.PreconditionViolationException;

/**
 * @since 5.0
 */
@API(Internal)
public final class TestRuleAnnotatedMemberFactory {

	///CLOVER:OFF
	private TestRuleAnnotatedMemberFactory() {
		/* no-op */
	}
	///CLOVER:ON

	public static TestRuleAnnotatedMember from(Object testInstance, Member member) {
		if (member instanceof Method) {
			return new TestRuleAnnotatedMethod(testInstance, (Method) member);
		}
		else if (member instanceof Field) {
			return new TestRuleAnnotatedField(testInstance, (Field) member);
		}
		throw new PreconditionViolationException(
			String.format("Unsupported Member type [%s] for TestRule. Member must be of type %s or %s", member,
				Method.class.getName(), Field.class.getName()));
	}

}
