/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine.vintage.rulesupport;

import org.junit.rules.ExternalResource;

public class ExternalResourceAdapter extends AbstractTestRuleAdapter {

	private final ExternalResource target;

	public ExternalResourceAdapter(ExternalResource target) {
		this.target = target;
	}

	@Override
	public void before() {
		super.executeMethod("before", this.target);
	}

	@Override
	public void after() {
		super.executeMethod("after", this.target);
	}

}
