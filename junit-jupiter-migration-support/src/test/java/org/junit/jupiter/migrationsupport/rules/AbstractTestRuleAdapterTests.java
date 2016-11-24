/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.migrationsupport.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.migrationsupport.rules.adapter.AbstractTestRuleAdapter;
import org.junit.jupiter.migrationsupport.rules.member.AbstractRuleAnnotatedMember;
import org.junit.jupiter.migrationsupport.rules.member.RuleAnnotatedMember;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.util.PreconditionViolationException;
import org.junit.rules.ErrorCollector;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;
import org.junit.rules.Verifier;

public class AbstractTestRuleAdapterTests {

	@Test
	void constructionWithAssignableArgumentsIsSuccessful() {
		new TestableTestRuleAdapter(new SimpleRuleAnnotatedMember(new ErrorCollector()), Verifier.class);
	}

	@Test
	void constructionWithUnassignableArgumentsFails() {
		PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
			() -> new TestableTestRuleAdapter(new SimpleRuleAnnotatedMember(new TemporaryFolder()), Verifier.class));

		assertEquals(exception.getMessage(),
			"class org.junit.rules.Verifier is not assignable from class org.junit.rules.TemporaryFolder");
	}

	@Test
	void exceptionsDuringMethodLookupAreWrappedAndThrown() {
		AbstractTestRuleAdapter adapter = new AbstractTestRuleAdapter(
			new SimpleRuleAnnotatedMember(new ErrorCollector()), Verifier.class) {
			@Override
			public void before() {
				super.executeMethod("foo");
			}
		};

		JUnitException exception = assertThrows(JUnitException.class, adapter::before);

		assertEquals(exception.getMessage(),
			"Error while looking up method to call via reflection for class org.junit.rules.ErrorCollector");
		assertEquals(NoSuchMethodException.class, exception.getCause().getClass());
	}

	private static class TestableTestRuleAdapter extends AbstractTestRuleAdapter {

		TestableTestRuleAdapter(RuleAnnotatedMember annotatedMember, Class<? extends TestRule> adapteeClass) {
			super(annotatedMember, adapteeClass);
		}
	}

	private static class SimpleRuleAnnotatedMember extends AbstractRuleAnnotatedMember {

		SimpleRuleAnnotatedMember(TestRule testRule) {
			super.testRuleInstance = testRule;
		}

	}

}
