/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.platform.commons.PreconditionViolationException;

public class ListenerRegistryTests {

	@Test
	void registerWithNullArray() {
		var registry = ListenerRegistry.create(l -> l.get(0));

		var exception = assertThrows(PreconditionViolationException.class, () -> registry.addAll((Object[]) null));

		assertThat(exception).hasMessageContaining("listeners array must not be null or empty");
	}

	@Test
	void registerWithEmptyArray() {
		var registry = ListenerRegistry.create(l -> l.get(0));

		var exception = assertThrows(PreconditionViolationException.class, registry::addAll);

		assertThat(exception).hasMessageContaining("listeners array must not be null or empty");
	}

	@Test
	void registerWithArrayContainingNullElements() {
		var registry = ListenerRegistry.create(l -> l.get(0));

		var exception = assertThrows(PreconditionViolationException.class,
			() -> registry.addAll(new Object[] { null }));

		assertThat(exception).hasMessageContaining("individual listeners must not be null");
	}
}
