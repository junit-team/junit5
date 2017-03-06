/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.launcher.core;

import static org.junit.platform.commons.meta.API.Usage.Experimental;

import org.junit.platform.commons.meta.API;
import org.junit.platform.commons.util.PreconditionViolationException;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.TestExecutionListener;

/**
 * Factory for creating {@link Launcher} instances by invoking {@link #create()}.
 *
 * <p>Test engines are discovered at runtime using the
 * {@link java.util.ServiceLoader ServiceLoader} facility. For that purpose, a
 * text file named {@code META-INF/services/org.junit.platform.engine.TestEngine}
 * has to be added to the engine's JAR file in which the fully qualified name
 * of the implementation class of the {@link org.junit.platform.engine.TestEngine}
 * interface is declared.
 *
 * <p>{@link org.junit.platform.launcher.TestExecutionListener}s discovered at
 * runtime via the {@link java.util.ServiceLoader ServiceLoader} facility are
 * automatically registered with the {@link Launcher} created by the factory.
 * Users may register additional listeners using the
 * {@link Launcher#registerTestExecutionListeners(TestExecutionListener...)}
 * method on the created launcher instance.
 *
 * @since 1.0
 * @see Launcher
 */
@API(Experimental)
public class LauncherFactory {

	/**
	 * Factory method for creating a new {@link Launcher} using dynamically
	 * detected test engines.
	 *
	 * <p>All dynamically detected {@link org.junit.platform.launcher.TestExecutionListener}s
	 * are automatically registered in the created {@link Launcher} instance.
	 *
	 * @throws PreconditionViolationException if no test engines are detected
	 */
	public static Launcher create() throws PreconditionViolationException {
		Launcher launcher = new DefaultLauncher(new ServiceLoaderTestEngineRegistry().loadTestEngines());
		for (TestExecutionListener listener : new ServiceLoaderTestExecutionListenerRegistry().loadListeners()) {
			launcher.registerTestExecutionListeners(listener);
		}
		return launcher;
	}

}
