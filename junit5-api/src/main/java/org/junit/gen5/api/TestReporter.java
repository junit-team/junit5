/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.api;

import org.junit.gen5.commons.reporting.ReportEntry;

/**
 * Variables of type {@code TestReporter} can be injected into
 * methods of test classes annotated with {@link BeforeEach},
 * {@link AfterEach}, and {@link Test} annotations, respectively.
 *
 * <p>Within these methods these references can then be used
 * to publish {@link ReportEntry} instances.
 *
 * @since 5.0
 */
@FunctionalInterface
public interface TestReporter {

	void publishEntry(ReportEntry entry);

}
