/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Abstract base class for unit tests that wish to test
 * {@link Object#equals(Object)} and {@link Object#hashCode()}.
 *
 * @since 1.3
 */
public abstract class AbstractEqualsAndHashCodeTests {

	protected final <T> void assertEqualsAndHashCode(T equal1, T equal2, T different) {
		assertThat(equal1).isNotNull();
		assertThat(equal2).isNotNull();
		assertThat(different).isNotNull();

		assertThat(equal1).isNotSameAs(equal2);
		assertThat(equal1).isNotNull();
		assertThat(equal1).isNotEqualTo(new Object());
		assertThat(equal1).isNotEqualTo(different);
		assertThat(different).isNotEqualTo(equal1);
		assertThat(different).isNotEqualTo(equal2);
		assertThat(equal1.hashCode()).isNotEqualTo(different.hashCode());

		assertThat(equal1).isEqualTo(equal1);
		assertThat(equal1).isEqualTo(equal2);
		assertThat(equal2).isEqualTo(equal1);
		assertThat(equal1.hashCode()).isEqualTo(equal2.hashCode());
	}

}
