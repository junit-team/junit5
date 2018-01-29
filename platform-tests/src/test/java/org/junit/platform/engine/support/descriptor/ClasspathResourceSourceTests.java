/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.support.descriptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.PreconditionViolationException;

/**
 * Unit tests for {@link ClasspathResourceSource}.
 *
 * @since 1.0
 */
class ClasspathResourceSourceTests extends AbstractTestSourceTests {

	private static final String FOO_RESOURCE = "test/foo.xml";
	private static final String BAR_RESOURCE = "/config/bar.json";

	@Test
	void preconditions() {
		assertThrows(PreconditionViolationException.class, () -> ClasspathResourceSource.from(null));
		assertThrows(PreconditionViolationException.class, () -> ClasspathResourceSource.from(""));
		assertThrows(PreconditionViolationException.class, () -> ClasspathResourceSource.from("   "));
	}

	@Test
	void resourceWithoutPosition() throws Exception {
		ClasspathResourceSource source = ClasspathResourceSource.from(FOO_RESOURCE);

		assertThat(source.getClasspathResourceName()).isEqualTo(FOO_RESOURCE);
		assertThat(source.getPosition()).isEmpty();
	}

	@Test
	void resourceWithLeadingSlashWithoutPosition() throws Exception {
		ClasspathResourceSource source = ClasspathResourceSource.from("/" + FOO_RESOURCE);

		assertThat(source.getClasspathResourceName()).isEqualTo(FOO_RESOURCE);
		assertThat(source.getPosition()).isEmpty();
	}

	@Test
	void resourceWithPosition() throws Exception {
		FilePosition position = FilePosition.from(42, 23);
		ClasspathResourceSource source = ClasspathResourceSource.from(FOO_RESOURCE, position);

		assertThat(source.getClasspathResourceName()).isEqualTo(FOO_RESOURCE);
		assertThat(source.getPosition()).hasValue(position);
	}

	@Test
	void equalsAndHashCode() {
		assertEqualsAndHashCode(ClasspathResourceSource.from(FOO_RESOURCE), ClasspathResourceSource.from(FOO_RESOURCE),
			ClasspathResourceSource.from(BAR_RESOURCE));

		FilePosition position = FilePosition.from(42, 23);
		assertEqualsAndHashCode(ClasspathResourceSource.from(FOO_RESOURCE, position),
			ClasspathResourceSource.from(FOO_RESOURCE, position), ClasspathResourceSource.from(BAR_RESOURCE, position));
	}

}
