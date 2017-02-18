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

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

/**
 * @see http://junit.org/junit4/javadoc/latest/org/junit/rules/Timeout.html
 */
public class TimeoutVintageDemo {

	@Rule
	public Timeout globalTimeout = new Timeout(20);

	@Test
	//tag::user_guide[]
	@Ignore
	//end::user_guide[]
	public void run1() throws InterruptedException {
		Thread.sleep(100);
	}

	@Test
	//tag::user_guide[]
	@Ignore
	//end::user_guide[]
	public void infiniteLoop() {
		while (true) {
		}
	}

}
//end::user_guide[]
