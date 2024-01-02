/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.discovery;

import static org.apiguardian.api.API.Status.STABLE;

import java.util.Objects;

import org.apiguardian.api.API;
import org.junit.platform.commons.util.ToStringBuilder;
import org.junit.platform.engine.DiscoverySelector;

/**
 * A {@link DiscoverySelector} that selects a module name so that
 * {@link org.junit.platform.engine.TestEngine TestEngines} can discover
 * tests or containers based on modules.
 *
 * @since 1.1
 * @see DiscoverySelectors#selectModule(String)
 * @see DiscoverySelectors#selectModules(java.util.Set)
 */
@API(status = STABLE, since = "1.1")
public class ModuleSelector implements DiscoverySelector {

	private final String moduleName;

	ModuleSelector(String moduleName) {
		this.moduleName = moduleName;
	}

	/**
	 * Get the selected module name.
	 */
	public String getModuleName() {
		return this.moduleName;
	}

	/**
	 * @since 1.3
	 */
	@API(status = STABLE, since = "1.3")
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ModuleSelector that = (ModuleSelector) o;
		return Objects.equals(this.moduleName, that.moduleName);
	}

	/**
	 * @since 1.3
	 */
	@API(status = STABLE, since = "1.3")
	@Override
	public int hashCode() {
		return this.moduleName.hashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("moduleName", this.moduleName).toString();
	}

}
