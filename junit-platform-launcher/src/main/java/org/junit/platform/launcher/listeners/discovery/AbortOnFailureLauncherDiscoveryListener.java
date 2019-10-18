/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher.listeners.discovery;

import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.util.ExceptionUtils;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.SelectorResolutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.UniqueIdSelector;
import org.junit.platform.launcher.EngineDiscoveryResult;
import org.junit.platform.launcher.LauncherDiscoveryListener;

class AbortOnFailureLauncherDiscoveryListener implements LauncherDiscoveryListener {

	@Override
	public void engineDiscoveryFinished(UniqueId engineId, EngineDiscoveryResult result) {
		result.getThrowable().ifPresent(ExceptionUtils::throwAsUncheckedException);
	}

	@Override
	public void selectorProcessed(UniqueId engineId, DiscoverySelector selector, SelectorResolutionResult result) {
		if (result.getStatus() == SelectorResolutionResult.Status.FAILED) {
			throw new JUnitException(selector + " resolution failed", result.getThrowable().orElse(null));
		}
		if (result.getStatus() == SelectorResolutionResult.Status.UNRESOLVED && selector instanceof UniqueIdSelector) {
			UniqueId uniqueId = ((UniqueIdSelector) selector).getUniqueId();
			if (uniqueId.hasPrefix(engineId)) {
				throw new JUnitException(selector + " could not be resolved");
			}
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		return getClass() == obj.getClass();
	}

	@Override
	public int hashCode() {
		return 0;
	}

}
