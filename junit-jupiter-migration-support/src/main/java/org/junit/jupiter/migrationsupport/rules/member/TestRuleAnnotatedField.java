/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.migrationsupport.rules.member;

import java.lang.reflect.Field;

import org.junit.platform.commons.util.ExceptionUtils;
import org.junit.rules.TestRule;

/**
 * @since 5.0
 */
class TestRuleAnnotatedField extends AbstractTestRuleAnnotatedMember {

	TestRuleAnnotatedField(Object testInstance, Field field) {
		super(retrieveTestRule(testInstance, field));
	}

	private static TestRule retrieveTestRule(Object testInstance, Field field) {
		try {
			if (!field.isAccessible()) {
				field.setAccessible(true);
			}
			return (TestRule) field.get(testInstance);
		}
		catch (IllegalAccessException exception) {
			throw ExceptionUtils.throwAsUncheckedException(exception);
		}
	}

}
