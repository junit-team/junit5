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

import java.lang.reflect.Method;

import org.junit.jupiter.api.extension.TestExtensionContext;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.rules.ExternalResource;

class RuleAnnotatedMethod implements RuleAnnotatedMember {

	private ExternalResource testRuleInstance;

	RuleAnnotatedMethod(TestExtensionContext context, Method member) {
		Object testInstance = context.getTestInstance();

		//no args
		this.testRuleInstance = (ExternalResource) ReflectionUtils.invokeMethod(member, testInstance);
	}

	@Override
	public ExternalResource getTestRuleInstance() {
		return this.testRuleInstance;
	}

}
