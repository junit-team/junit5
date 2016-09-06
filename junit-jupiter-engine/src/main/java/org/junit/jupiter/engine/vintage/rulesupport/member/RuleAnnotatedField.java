/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine.vintage.rulesupport.member;

import java.lang.reflect.Field;

import org.junit.jupiter.engine.vintage.rulesupport.member.AbstractRuleAnnotatedMember;
import org.junit.rules.TestRule;

public class RuleAnnotatedField extends AbstractRuleAnnotatedMember {

	public RuleAnnotatedField(Object testInstance, Field testRuleField) {
		try {
			this.testRuleInstance = (TestRule) testRuleField.get(testInstance);
		}
		catch (IllegalAccessException e) {
			// TODO: decide whether this should be logged
			e.printStackTrace();
		}
	}

}
