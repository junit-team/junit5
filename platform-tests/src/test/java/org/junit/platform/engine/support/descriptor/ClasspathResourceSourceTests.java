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
 * Unit tests for {@link ClasspathResourceSource}.
 *
 * @since 1.0
 */
class ClasspathResourceSourceTests extends AbstractTestSourceTests {

	private static final String FOO_RESOURCE = "test/foo.xml";
	private static final String BAR_RESOURCE = "/config/bar.json";

	@Test
	void preconditions() {
		assertThrows(PreconditionViolationException.class, () -> new ClasspathResourceSource(null));
		assertThrows(PreconditionViolationException.class, () -> new ClasspathResourceSource(""));
		assertThrows(PreconditionViolationException.class, () -> new ClasspathResourceSource("   "));
	}

	@Test
	void resourceWithoutPosition() throws Exception {
		ClasspathResourceSource source = new ClasspathResourceSource(FOO_RESOURCE);

		assertThat(source.getClasspathResourceName()).isEqualTo(FOO_RESOURCE);
		assertThat(source.getPosition()).isEmpty();
	}

	@Test
	void resourceWithLeadingSlashWithoutPosition() throws Exception {
		ClasspathResourceSource source = new ClasspathResourceSource("/" + FOO_RESOURCE);

		assertThat(source.getClasspathResourceName()).isEqualTo(FOO_RESOURCE);
		assertThat(source.getPosition()).isEmpty();
	}

	@Test
	void resourceWithPosition() throws Exception {
		FilePosition position = new FilePosition(42, 23);
		ClasspathResourceSource source = new ClasspathResourceSource(FOO_RESOURCE, position);

		assertThat(source.getClasspathResourceName()).isEqualTo(FOO_RESOURCE);
		assertThat(source.getPosition()).hasValue(position);
	}

	@Test
	void equalsAndHashCode() {
		assertEqualsAndHashCode(new ClasspathResourceSource(FOO_RESOURCE), new ClasspathResourceSource(FOO_RESOURCE),
			new ClasspathResourceSource(BAR_RESOURCE));

		FilePosition position = new FilePosition(42, 23);
		assertEqualsAndHashCode(new ClasspathResourceSource(FOO_RESOURCE, position),
			new ClasspathResourceSource(FOO_RESOURCE, position), new ClasspathResourceSource(BAR_RESOURCE, position));
	}

}
