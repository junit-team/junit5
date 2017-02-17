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

// @formatter:off
//tag::user_guide[]
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.Test;

/**
 * @see http://junit.org/junit4/javadoc/latest/org/junit/rules/ErrorCollector.html
 * @see http://junit.org/junit5/docs/snapshot/api/org/junit/jupiter/api/Assertions.html#assertAll-org.junit.jupiter.api.function.Executable...-
 */
public class ErrorCollectorJupiterDemo {

	@Test
	//end::user_guide[]
    @extensions.ExpectToFail
	//tag::user_guide[]
	public void example() {
		assertAll(
			() -> { throw new AssertionError(new Throwable("first thing went wrong")); },
			() -> { throw new AssertionError(new Throwable("second thing went wrong")); },
			() -> assertThat("ERROR! - something is broke", not(containsString("ERROR!")))
		);
	}

}
//end::user_guide[]
// @formatter:on
