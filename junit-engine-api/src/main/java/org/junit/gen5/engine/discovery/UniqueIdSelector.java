/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.discovery;

import static org.junit.gen5.commons.meta.API.Usage.Experimental;

import org.junit.gen5.commons.meta.API;
import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.engine.DiscoverySelector;
import org.junit.gen5.engine.UniqueId;

/**
 * A {@link DiscoverySelector} that selects a {@link UniqueId} so that
 * {@link org.junit.gen5.engine.TestEngine TestEngines} can discover
 * tests or containers based on unique IDs.
 *
 * @since 5.0
 */
@API(Experimental)
public class UniqueIdSelector implements DiscoverySelector {

	/**
	 * Create a {@code UniqueIdSelector} for the supplied {@link UniqueId}.
	 *
	 * @param uniqueId the {@code UniqueId} to select; never {@code null}
	 */
	public static UniqueIdSelector forUniqueId(UniqueId uniqueId) {
		Preconditions.notNull(uniqueId, "UniqueId must not be null");
		return forUniqueId(uniqueId.toString());
	}

	/**
	 * Create a {@code UniqueIdSelector} for the supplied unique ID.
	 *
	 * @param uniqueId the unique ID to select; never {@code null} or empty
	 */
	public static UniqueIdSelector forUniqueId(String uniqueId) {
		Preconditions.notBlank(uniqueId, "Unique ID must not be null or empty");
		return new UniqueIdSelector(uniqueId);
	}

	private final String uniqueId;

	private UniqueIdSelector(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	/**
	 * Get the selected unique ID.
	 */
	public String getUniqueId() {
		return this.uniqueId;
	}

}
