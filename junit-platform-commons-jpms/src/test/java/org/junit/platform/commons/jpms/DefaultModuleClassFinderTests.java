/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.commons.jpms;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

/**
 * Default module reference scanner tests.
 *
 * @since 1.1
 */
class DefaultModuleClassFinderTests {

	@Test
	void toStringIsNotNull() {
		assertNotNull(new DefaultModuleClassFinder().toString());
	}

}
