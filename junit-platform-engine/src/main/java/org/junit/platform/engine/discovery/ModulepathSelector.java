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

import org.apiguardian.api.API;
import org.junit.platform.commons.util.ToStringBuilder;
import org.junit.platform.engine.DiscoverySelector;

/**
 * A {@link DiscoverySelector} that selects all modules on the module-path so that
 * {@link org.junit.platform.engine.TestEngine TestEngines} can discover
 * tests or containers based on all resolved modules.
 *
 * @since 1.1
 */
@API(status = EXPERIMENTAL, since = "1.1")
public class ModulepathSelector implements DiscoverySelector {

	ModulepathSelector() {
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).toString();
	}

}
