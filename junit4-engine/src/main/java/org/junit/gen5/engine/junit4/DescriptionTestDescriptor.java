/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit4;

import org.junit.runner.Description;

class DescriptionTestDescriptor extends JUnit4TestDescriptor {

	final Description description;

	DescriptionTestDescriptor(Description description) {
		// TODO Use unique ID if set, too
		super(ENGINE_ID + ":" + description.getDisplayName());
		this.description = description;

	}

	@Override
	public Description getDescription() {
		return description;
	}
}