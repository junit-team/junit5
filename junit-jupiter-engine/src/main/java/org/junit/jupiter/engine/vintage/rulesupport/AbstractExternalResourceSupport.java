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

import java.lang.reflect.Member;
import java.util.List;
import java.util.function.Consumer;

import org.junit.Rule;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.TestExtensionContext;
import org.junit.rules.ExternalResource;

public abstract class AbstractExternalResourceSupport implements BeforeEachCallback, AfterEachCallback {

	private final Class<Rule> annotationType = Rule.class;
	private final Class<ExternalResource> ruleType = ExternalResource.class;

	protected abstract RuleAnnotatedMember createRuleAnnotatedMember(TestExtensionContext context, Member member);

	protected abstract List<Member> findRuleAnnotatedMembers(Object testInstance);

	protected Class<Rule> getAnnotationType() {
		return this.annotationType;
	}

	protected Class<ExternalResource> getRuleType() {
		return this.ruleType;
	}

	@Override
	public void beforeEach(TestExtensionContext context) throws Exception {
		this.invokeAppropriateMethodOnRuleAnnotatedMembers(context, GenericBeforeAndAfterAdvice::before);
	}

	@Override
	public void afterEach(TestExtensionContext context) throws Exception {
		this.invokeAppropriateMethodOnRuleAnnotatedMembers(context, GenericBeforeAndAfterAdvice::after);
	}

	protected void invokeAppropriateMethodOnRuleAnnotatedMembers(TestExtensionContext context,
			Consumer<GenericBeforeAndAfterAdvice> methodCaller) {
		List<Member> members = this.findRuleAnnotatedMembers(context.getTestInstance());

		// @formatter:off
        members.stream()
                .map(member -> this.createRuleAnnotatedMember(context, member))
                .map(annotatedMember -> new ExternalResourceAdapter(annotatedMember.getTestRuleInstance()))
		        .forEach(methodCaller::accept);
        // @formatter:on
	}

}
