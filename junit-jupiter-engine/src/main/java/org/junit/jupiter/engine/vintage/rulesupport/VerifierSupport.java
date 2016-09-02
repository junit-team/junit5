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

import java.util.function.Function;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.TestExtensionContext;
import org.junit.rules.TestRule;

public class VerifierSupport implements AfterEachCallback {

	private final Class<? extends TestRule> ruleType = TestRule.class;
	private final Function<RuleAnnotatedMember, AbstractTestRuleAdapter> adapterGenerator = annotatedMember -> new VerifierAdapter(
		annotatedMember.getTestRuleInstance());

	private AbstractTestRuleSupport fieldSupport = new TestRuleFieldSupport(ruleType, adapterGenerator);
	private AbstractTestRuleSupport methodSupport = new TestRuleMethodSupport(ruleType, adapterGenerator);

	@Override
	public void afterEach(TestExtensionContext context) throws Exception {
		this.fieldSupport.afterEach(context);
		this.methodSupport.afterEach(context);
	}

}
