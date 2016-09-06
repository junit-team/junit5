/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine.vintage.rulesupport.adapter;

import org.junit.jupiter.engine.vintage.rulesupport.member.RuleAnnotatedMember;

public class VerifierAdapter extends AbstractTestRuleAdapter {

	public VerifierAdapter(RuleAnnotatedMember annotatedMember) {
		super(annotatedMember);
	}

	@Override
	public void after() {
		super.executeMethod("verify", this.target);
	}

}
