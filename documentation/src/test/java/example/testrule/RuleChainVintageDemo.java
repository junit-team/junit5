/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package example.testrule;

// tag:user_guide
// formatter:on
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

public class RuleChainVintageDemo {

	@Rule
	public RuleChain chain = RuleChain.outerRule(new LoggingRule("outer rule")).around(
		new LoggingRule("middle rule")).around(new LoggingRule("inner rule"));

	@Test
	public void example() {
		assertTrue(true);
	}

}
// @formatter:off
// end:user_guide
