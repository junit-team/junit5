/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.descriptor;

import static org.apiguardian.api.API.Status.INTERNAL;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import org.apiguardian.api.API;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;

/**
 * Filter for dynamic descendants of {@link TestDescriptor TestDescriptors} that
 * implement {@link Filterable}.
 *
 * @since 5.1
 * @see Filterable
 */
@API(status = INTERNAL, since = "5.1")
public class DynamicDescendantFilter implements Predicate<UniqueId> {

	private final Set<UniqueId> allowed = new HashSet<>();
	private Mode mode = Mode.EXPLICIT;

	public void allow(UniqueId uniqueId) {
		if (this.mode == Mode.EXPLICIT) {
			this.allowed.add(uniqueId);
		}
	}

	public void allowAll() {
		this.mode = Mode.ALLOW_ALL;
		this.allowed.clear();
	}

	@Override
	public boolean test(UniqueId uniqueId) {
		return allowed.isEmpty() || allowed.stream().anyMatch(allowedUniqueId -> isAllowed(uniqueId, allowedUniqueId));
	}

	private boolean isAllowed(UniqueId currentUniqueId, UniqueId allowedUniqueId) {
		return allowedUniqueId.hasPrefix(currentUniqueId) || currentUniqueId.hasPrefix(allowedUniqueId);
	}

	private enum Mode {
		EXPLICIT, ALLOW_ALL
	}
}
