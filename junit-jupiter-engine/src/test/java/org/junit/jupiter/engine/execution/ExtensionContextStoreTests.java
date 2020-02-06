/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.execution;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.ExtensionContextException;

/**
 * Unit tests for {@link NamespaceAwareStore} and {@link ExtensionValuesStore}.
 *
 * @since 5.5
 * @see ExtensionContextStoreConcurrencyTests
 * @see ExtensionValuesStoreTests
 */
class ExtensionContextStoreTests {

	private static final String KEY = "key";
	private static final String VALUE = "value";

	private final ExtensionValuesStore parentStore = new ExtensionValuesStore(null);
	private final ExtensionValuesStore localStore = new ExtensionValuesStore(parentStore);
	private final Store store = new NamespaceAwareStore(localStore, Namespace.GLOBAL);

	@Test
	void getOrDefaultWithNoValuePresent() {
		assertThat(store.get(KEY)).isNull();

		assertThat(store.getOrDefault(KEY, boolean.class, true)).isTrue();
		assertThat(store.getOrDefault(KEY, String.class, VALUE)).isEqualTo(VALUE);
	}

	@Test
	void getOrDefaultRequestingIncompatibleType() {
		localStore.put(Namespace.GLOBAL, KEY, VALUE);
		assertThat(store.get(KEY)).isEqualTo(VALUE);

		Exception exception = assertThrows(ExtensionContextException.class,
			() -> store.getOrDefault(KEY, boolean.class, true));
		assertThat(exception).hasMessageContaining("is not of required type");
	}

	@Test
	void getOrDefaultWithValueInLocalStore() {
		localStore.put(Namespace.GLOBAL, KEY, VALUE);
		assertThat(store.get(KEY)).isEqualTo(VALUE);

		assertThat(store.getOrDefault(KEY, String.class, VALUE)).isEqualTo(VALUE);
	}

	@Test
	void getOrDefaultWithValueInParentStore() {
		parentStore.put(Namespace.GLOBAL, KEY, VALUE);
		assertThat(store.get(KEY)).isEqualTo(VALUE);

		assertThat(store.getOrDefault(KEY, String.class, VALUE)).isEqualTo(VALUE);
	}

}
