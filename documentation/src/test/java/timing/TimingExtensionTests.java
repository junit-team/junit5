/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package timing;

import org.junit.gen5.api.Test;
import org.junit.gen5.api.extension.ExtendWith;
import org.junit.gen5.junit4.runner.JUnit5;
import org.junit.runner.RunWith;

/**
 * Tests that demonstrate the example {@link TimingExtension}.
 *
 * @since 5.0
 */
@ExtendWith(TimingExtension.class)
class TimingExtensionTests {

	@Test
	void sleep20ms() throws Exception {
		Thread.sleep(20);
	}

	@Test
	void sleep50ms() throws Exception {
		Thread.sleep(50);
	}

}
