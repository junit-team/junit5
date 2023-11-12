/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.jfr;

import jdk.jfr.Category;
import jdk.jfr.Event;
import jdk.jfr.StackTrace;

@Category({ "JUnit", "JUnit Platform" })
@StackTrace(value = false)
@SuppressWarnings("Since15") // We use JFR Polyfill
public abstract class JUnitPlatformEvent extends Event {
}
