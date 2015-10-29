/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine;

/**
 * @author Sam Brannen
 * @since 5.0
 */
public interface TestDescriptor {

	/**
	 * Get the unique identifier (UID) for the described test.
	 *
	 * <p>Uniqueness must be guaranteed across an entire test plan,
	 * regardless of how many engines are used behind the scenes.
	 *
	 * <p>The ID is simply the concatenation of the {@linkplain #getEngineId engine ID}
	 * and the {@linkplain #getTestId test ID} separated by a colon ({@code ":"})
	 * &mdash; for example, {@code myEngine:test-description-unique-within-my-engine}.
	 */
	default String getUniqueId() {
		return String.format("%s:%s", getEngineId(), getTestId());
	};

	String getEngineId();

	String getTestId();

	String getDisplayName();

	TestDescriptor getParent();

	boolean isTest();

}
