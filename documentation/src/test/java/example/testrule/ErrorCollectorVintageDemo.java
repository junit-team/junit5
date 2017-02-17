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
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

/**
 * @see http://junit.org/junit4/javadoc/latest/org/junit/rules/ErrorCollector.html
 */
public class ErrorCollectorVintageDemo {

	@Rule
	public ErrorCollector collector = new ErrorCollector();

	@Test
	//end::user_guide[]
	@Ignore
	//tag::user_guide[]
	public void example() {
		collector.addError(new Throwable("first thing went wrong"));
		collector.addError(new Throwable("second thing went wrong"));
		collector.checkThat("ERROR! - something is broke", not(containsString("ERROR!")));
		// all lines will run, and then a combined failure logged at the end.
	}

}
//end::user_guide[]
