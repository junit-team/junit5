/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.engine.discovery;

import static org.junit.platform.commons.meta.API.Status.STABLE;

import org.junit.platform.commons.meta.API;
import org.junit.platform.commons.util.ToStringBuilder;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.UniqueId;

/**
 * A {@link DiscoverySelector} that selects a {@link UniqueId} so that
 * {@link org.junit.platform.engine.TestEngine TestEngines} can discover
 * tests or containers based on unique IDs.
 *
 * @since 1.0
 */
@API(status = STABLE)
public class UniqueIdSelector implements DiscoverySelector {

	private final UniqueId uniqueId;

	UniqueIdSelector(UniqueId uniqueId) {
		this.uniqueId = uniqueId;
	}

	/**
	 * Get the selected {@link UniqueId}.
	 */
	public UniqueId getUniqueId() {
		return this.uniqueId;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("uniqueId", this.uniqueId).toString();
	}

}
