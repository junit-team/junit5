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

import org.junit.Rule;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.rules.ErrorCollector;

@ExtendWith(VerifierSupport.class)
public class VerifierSupportForErrorCollectorFieldTests {

	@Rule
	public ErrorCollector collector = new ErrorCollector();

	// TODO: this test does expose the correct behavior
	// - is there a way to formalize this in junit 5 without failing the test?
	// the expected org.junit.internal.runners.model.MultipleFailureException is only thrown
	// after the test method itself...
	// @Test
	void addingTwoThrowablesToErrorCollectorFailsLate() {
		collector.addError(new Throwable("first thing went wrong"));
		collector.addError(new Throwable("second thing went wrong"));
	}

}
