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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @see http://junit.org/junit4/javadoc/latest/org/junit/rules/ExpectedException.html
 */
public class ExpectedExceptionVintageDemo {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void throwsNothing() {
		// no exception expected, none thrown: passes.
	}

	@Test
	public void throwsExceptionWithSpecificType() {
		thrown.expect(NullPointerException.class);
		throw new NullPointerException();
	}

}
