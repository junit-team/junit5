/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.support.descriptor;

import static org.junit.gen5.commons.meta.API.Usage.Experimental;

import org.junit.gen5.commons.meta.API;
import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.engine.UniqueId;

/**
 * @since 5.0
 */
@API(Experimental)
public class EngineDescriptor extends AbstractTestDescriptor {

	private final String displayName;

	public EngineDescriptor(UniqueId uniqueId, String displayName) {
		super(uniqueId);
		this.displayName = Preconditions.notBlank(displayName, "display name must not be null or empty");
	}

	@Override
	public final String getDisplayName() {
		return this.displayName;
	}

	@Override
	public final boolean isTest() {
		return false;
	}

	@Override
	public final boolean isContainer() {
		return true;
	}

}
