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

import java.lang.reflect.Method;

import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.rules.TestRule;

/**
 * @since 5.0
 */
class TestRuleAnnotatedMethod extends AbstractTestRuleAnnotatedMember {

	TestRuleAnnotatedMethod(Object testInstance, Method method) {
		super((TestRule) ReflectionUtils.invokeMethod(method, testInstance));
	}

}
