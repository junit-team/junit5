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

import jdk.jfr.Label;
import jdk.jfr.Name;

@SuppressWarnings("Since15") // We use JFR Polyfill
public abstract class ExclusiveResourceEvent extends JUnitPlatformEvent {

	String key;
	Mode mode;
	String owner;

	public void initialize(String key, Mode mode, String owner) {
		this.key = key;
		this.mode = mode;
		this.owner = owner;
	}

	@Label("Exclusive Resource Acquiring")
	@Name("org.junit.ExclusiveResourceAcquiring")
	public static class ExclusiveResourceAcquiringEvent extends ExclusiveResourceEvent {

	}

	@Label("Exclusive Resource Acquired")
	@Name("org.junit.ExclusiveResourceAcquired")
	public static class ExclusiveResourceAcquiredEvent extends ExclusiveResourceEvent {

	}

	@Label("Exclusive Resource Released")
	@Name("org.junit.ExclusiveResourceReleased")
	public static class ExclusiveResourceReleasedEvent extends ExclusiveResourceEvent {

	}

	public enum Mode {
		READ, READ_WRITE;
	}
}
