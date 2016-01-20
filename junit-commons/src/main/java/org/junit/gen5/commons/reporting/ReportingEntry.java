/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.commons.reporting;

import java.util.Map;

public class ReportingEntry {

	private final Map<String, String> values;

	/**
	 * @param values the values to be published
	 */
	public ReportingEntry(Map<String, String> values) {
		this.values = values;
	}

	public Map<String, String> getValues() {
		return values;
	}
}
