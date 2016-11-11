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

import static org.junit.platform.commons.meta.API.Usage.Internal;

import java.lang.reflect.Field;

import org.junit.platform.commons.meta.API;
import org.junit.rules.TestRule;

@API(Internal)
public class RuleAnnotatedField extends AbstractRuleAnnotatedMember {

	public RuleAnnotatedField(Object testInstance, Field testRuleField) {
		try {
			testRuleField.setAccessible(true);
			this.testRuleInstance = (TestRule) testRuleField.get(testInstance);
		}
		catch (IllegalAccessException e) {
			// TODO: decide whether this should be logged
			e.printStackTrace();
		}
	}

}
