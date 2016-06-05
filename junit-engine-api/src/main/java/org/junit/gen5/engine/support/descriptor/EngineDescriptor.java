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
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestEngine;
import org.junit.gen5.engine.UniqueId;

/**
 * An {@code EngineDescriptor} is a {@link TestDescriptor} for a specific
 * {@link TestEngine}.
 *
 * @since 5.0
 */
@API(Experimental)
public class EngineDescriptor extends AbstractTestDescriptor {

	private final String displayName;

	/**
	 * Create a new {@code EngineDescriptor} with the supplied {@link UniqueId}
	 * and display name.
	 *
	 * @param uniqueId the {@code UniqueId} for the described {@code TestEngine};
	 * never {@code null}
	 * @param displayName the display name for the  described {@code TestEngine};
	 * never {@code null} or blank
	 * @see TestEngine#getId()
	 * @see TestDescriptor#getDisplayName()
	 */
	public EngineDescriptor(UniqueId uniqueId, String displayName) {
		super(uniqueId);
		this.displayName = Preconditions.notBlank(displayName, "display name must not be null or blank");
	}

	@Override
	public final String getDisplayName() {
		return this.displayName;
	}

	/**
	 * Always returns {@code false}: a {@link TestEngine} is never a test.
	 *
	 * @see org.junit.gen5.engine.TestDescriptor#isTest()
	 * @see #isContainer()
	 */
	@Override
	public final boolean isTest() {
		return false;
	}

	/**
	 * Always returns {@code true}: a {@link TestEngine} is always a container.
	 *
	 * @see org.junit.gen5.engine.TestDescriptor#isContainer()
	 * @see #isTest()
	 */
	@Override
	public final boolean isContainer() {
		return true;
	}

}
