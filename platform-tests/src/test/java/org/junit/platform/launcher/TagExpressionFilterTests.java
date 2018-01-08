/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.PreconditionViolationException;

class TagExpressionFilterTests {

	@Test
	void rejectUnParsableTagExpressions() {
		String brokenTagExpression = "tag & ";
		RuntimeException expected = assertThrows(PreconditionViolationException.class,
			() -> TagExpressionFilter.includeMatching(brokenTagExpression));
		assertThat(expected).hasMessageStartingWith("Unable to parse tag expression [" + brokenTagExpression + "]");
	}

}
