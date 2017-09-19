/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.migrationsupport.rules.member;

import static org.apiguardian.api.API.Status.INTERNAL;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import org.apiguardian.api.API;
import org.junit.platform.commons.util.PreconditionViolationException;

/**
 * @since 5.0
 */
@API(status = INTERNAL, since = "5.0")
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
		if (member instanceof Field) {
			return new TestRuleAnnotatedField(testInstance, (Field) member);
		}
		throw new PreconditionViolationException(
			String.format("Unsupported Member type [%s] for TestRule. Member must be of type %s or %s", member,
				Method.class.getName(), Field.class.getName()));
	}

}
