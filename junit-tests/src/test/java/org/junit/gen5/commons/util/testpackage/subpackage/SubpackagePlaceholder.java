/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.commons.util.testpackage.subpackage;

import org.junit.gen5.api.Assertions;
import org.junit.gen5.api.Test;

/**
 * This class is intended for testing purposes only. Please do not remove!
 *
 * Warning: This class needs to be available, otherwise the package will
 * not be available in the compiled classes folder.
 */
public class SubpackagePlaceholder {
	private class NotANestedTestClassBecauseOfLimitedVisibility {
		@Test
		void test() {
			Assertions.fail("This must not be executed!");
		}
	}

	class NotANestedInnerTestClassBecauseOfMissingAnnotation {
		@Test
		void notATestBecauseTheClassIsNotATestClass() {
			Assertions.fail("This must not be executed!");
		}
	}
}
