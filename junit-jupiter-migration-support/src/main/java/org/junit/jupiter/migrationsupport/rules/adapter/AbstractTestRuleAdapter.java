/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.migrationsupport.rules.adapter;

import static org.junit.platform.commons.meta.API.Usage.Internal;

import java.lang.reflect.Method;

import org.junit.jupiter.migrationsupport.rules.member.RuleAnnotatedMember;
import org.junit.platform.commons.meta.API;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.rules.TestRule;

@API(Internal)
public abstract class AbstractTestRuleAdapter implements GenericBeforeAndAfterAdvice {

	protected final TestRule target;
	protected final Class<? extends TestRule> adapteeClass;

	public AbstractTestRuleAdapter(RuleAnnotatedMember annotatedMember, Class<? extends TestRule> adapteeClass) {
		this.target = annotatedMember.getTestRuleInstance();
		this.adapteeClass = adapteeClass;

		this.failIfAdapteeClassIsNotAssignableFromTargetClass();
	}

	private void failIfAdapteeClassIsNotAssignableFromTargetClass() {
		if (!this.adapteeClass.isAssignableFrom(this.target.getClass()))
			throw new IllegalStateException(this.adapteeClass + " is not assignable from " + this.target.getClass());
	}

	protected void executeMethod(String name) {
		try {
			Method method = target.getClass().getDeclaredMethod(name);
			method.setAccessible(true);
			ReflectionUtils.invokeMethod(method, target);
		}
		catch (NoSuchMethodException | SecurityException e) {
			// TODO: decide whether this should be logged
			e.printStackTrace();
		}
	}

}
