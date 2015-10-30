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

import org.junit.gen5.engine.TestDescriptor;

final class DummyTestDescriptor implements TestDescriptor {

	@Override
	public boolean isTest() {
		return true;
	}

	@Override
	public String getUniqueId() {
		return "dummy";
	}

	@Override
	public TestDescriptor getParent() {
		return null;
	}

	@Override
	public String getDisplayName() {
		return "dummy";
	}
}