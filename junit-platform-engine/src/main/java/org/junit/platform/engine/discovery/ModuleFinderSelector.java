/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.discovery;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import org.apiguardian.api.API;
import org.junit.platform.commons.util.ToStringBuilder;
import org.junit.platform.engine.DiscoverySelector;

/**
 * A {@link DiscoverySelector} that selects all modules on the custom module-path
 * so that {@link org.junit.platform.engine.TestEngine TestEngines} can discover
 * tests or containers based on the custom module-path.
 *
 * @since 1.1
 */
@API(status = EXPERIMENTAL, since = "1.1")
public class ModuleFinderSelector implements DiscoverySelector {

	private final ClassLoader classLoader;
	private final String[] entries;

	ModuleFinderSelector(ClassLoader classLoader, String[] entries) {
		this.classLoader = classLoader;
		this.entries = entries;
	}

	public ClassLoader getClassLoader() {
		return classLoader;
	}

	public String[] getEntries() {
		return entries;
	}

	public Path[] getPaths() {
		return Arrays.stream(entries).map(Paths::get).toArray(Path[]::new);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("entries", this.entries).toString();
	}

}
