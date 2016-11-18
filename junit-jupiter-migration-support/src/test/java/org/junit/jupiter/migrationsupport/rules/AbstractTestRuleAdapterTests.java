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
		Throwable throwable = assertThrows(IllegalStateException.class,
			() -> new TestableTestRuleAdapter(new SimpleRuleAnnotatedMember(new TemporaryFolder()), Verifier.class));

		assertEquals(throwable.getMessage(),
			"class org.junit.rules.Verifier is not assignable from class org.junit.rules.TemporaryFolder");
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
