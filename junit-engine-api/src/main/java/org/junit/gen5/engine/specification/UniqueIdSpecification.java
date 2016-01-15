/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.specification;

import org.junit.gen5.engine.DiscoverySelector;
import org.junit.gen5.engine.DiscoverySelectorVisitor;

public class UniqueIdSpecification implements DiscoverySelector {
	private final String uniqueId;

	public UniqueIdSpecification(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	@Override
	public void accept(DiscoverySelectorVisitor visitor) {
		visitor.visitUniqueId(uniqueId);
	}

	public String getUniqueId() {
		return uniqueId;
	}
}
