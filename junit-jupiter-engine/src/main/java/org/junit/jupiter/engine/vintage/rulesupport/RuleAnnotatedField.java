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

import java.lang.reflect.Field;

import org.junit.jupiter.api.extension.TestExtensionContext;
import org.junit.rules.ExternalResource;

class RuleAnnotatedField implements RuleAnnotatedMember {

	private ExternalResource testRuleInstance;

	RuleAnnotatedField(TestExtensionContext context, Field testRuleField) {
		Object testInstance = context.getTestInstance();

		try {
			this.testRuleInstance = (ExternalResource) testRuleField.get(testInstance);
		}
		catch (IllegalAccessException e) {
			// TODO: decide whether this should be logged
			e.printStackTrace();
		}
	}

	@Override
	public ExternalResource getTestRuleInstance() {
		return this.testRuleInstance;
	}

}
