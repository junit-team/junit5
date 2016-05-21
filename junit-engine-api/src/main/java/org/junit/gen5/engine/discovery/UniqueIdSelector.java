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
import org.junit.gen5.engine.DiscoverySelector;
import org.junit.gen5.engine.UniqueId;

/**
 * @since 5.0
 */
@API(Experimental)
public class UniqueIdSelector implements DiscoverySelector {

	public static UniqueIdSelector forUniqueId(UniqueId uniqueId) {
		return forUniqueId(uniqueId.getUniqueString());
	}

	public static UniqueIdSelector forUniqueId(String uniqueId) {
		return new UniqueIdSelector(uniqueId);
	}

	private final String uniqueId;

	private UniqueIdSelector(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	public String getUniqueId() {
		return this.uniqueId;
	}

}
