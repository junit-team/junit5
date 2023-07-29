/*
 * Copyright 2015-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

module org.junit.vintage.reporting {
	requires transitive junit;
	requires static transitive org.apiguardian.api;
	exports org.junit.vintage.reporting;
	uses org.junit.vintage.reporting.VintageReportingService;
}
