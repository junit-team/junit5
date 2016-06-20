/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.engine.discovery;

import static org.junit.platform.commons.meta.API.Usage.Experimental;

import org.junit.platform.commons.meta.API;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.UniqueId;

/**
 * A {@link DiscoverySelector} that selects a {@link UniqueId} so that
 * {@link org.junit.platform.engine.TestEngine TestEngines} can discover
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
	public static UniqueIdSelector selectUniqueId(UniqueId uniqueId) {
		Preconditions.notNull(uniqueId, "UniqueId must not be null");
		return new UniqueIdSelector(uniqueId);
	}

	/**
	 * Create a {@code UniqueIdSelector} for the supplied unique ID.
	 *
	 * @param uniqueId the unique ID to select; never {@code null} or blank
	 */
	public static UniqueIdSelector selectUniqueId(String uniqueId) {
		Preconditions.notBlank(uniqueId, "Unique ID must not be null or blank");
		return new UniqueIdSelector(UniqueId.parse(uniqueId));
	}

	private final UniqueId uniqueId;

	private UniqueIdSelector(UniqueId uniqueId) {
		this.uniqueId = uniqueId;
	}

	/**
	 * Get the selected {@link UniqueId}.
	 */
	public UniqueId getUniqueId() {
		return this.uniqueId;
	}

}
