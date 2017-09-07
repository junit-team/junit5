/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.engine.support.descriptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.PreconditionViolationException;

/**
 * Unit tests for {@link FilePosition}.
 *
 * @since 1.0
 */
class FilePositionTests extends AbstractTestSourceTests {

	@Test
	void preconditions() {
		assertThrows(PreconditionViolationException.class, () -> FilePosition.from(-1));
		assertThrows(PreconditionViolationException.class, () -> FilePosition.from(0, -1));
	}

	@Test
	void filePositionFromLine() throws Exception {
		FilePosition filePosition = FilePosition.from(42);

		assertThat(filePosition.getLine()).isEqualTo(42);
		assertThat(filePosition.getColumn()).isEmpty();
	}

	@Test
	void filePositionFromLineAndColumn() throws Exception {
		FilePosition filePosition = FilePosition.from(42, 99);

		assertThat(filePosition.getLine()).isEqualTo(42);
		assertThat(filePosition.getColumn()).contains(99);
	}

	@Test
	void equalsAndHashCode() {
		FilePosition same = FilePosition.from(42, 99);
		FilePosition sameSame = FilePosition.from(42, 99);
		FilePosition different = FilePosition.from(1, 2);

		assertEqualsAndHashCode(same, sameSame, different);
	}

}
