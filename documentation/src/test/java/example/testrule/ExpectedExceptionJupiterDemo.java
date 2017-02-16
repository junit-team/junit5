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

//tag::user_guide[]
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * This is the JUnit 5 equivalent of the ExpectedException.
 *
 * @see http://junit.org/junit5/docs/current/api/org/junit/jupiter/api/Assertions.html#assertThrows-java.lang.Class-org.junit.jupiter.api.function.Executable-
 * @see http://junit.org/junit4/javadoc/latest/org/junit/rules/ExpectedException.html
 */
public class ExpectedExceptionJupiterDemo {

	@Test
	public void throwsNothing() {
		// no exception expected, none thrown: passes.
	}

	@Test
	public void throwsExceptionWithSpecificType() {
		assertThrows(NullPointerException.class, () -> {
			throw new NullPointerException();
		});
	}

}
//end::user_guide[]
