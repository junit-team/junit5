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

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.SelectorResolutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.UniqueIdSelector;
import org.junit.platform.launcher.LauncherDiscoveryListener;

class LoggingLauncherDiscoveryListener implements LauncherDiscoveryListener {

	private static final Logger logger = LoggerFactory.getLogger(LoggingLauncherDiscoveryListener.class);

	@Override
	public void engineDiscoveryStarted(UniqueId engineId) {
		logger.trace(() -> "Engine " + engineId + " has started discovering tests");
	}

	@Override
	public void engineDiscoveryFinished(UniqueId engineId, Optional<Throwable> failure) {
		if (failure.isPresent()) {
			logger.error(failure.get().getCause(), () -> failure.get().getMessage());
		}
		else {
			logger.trace(() -> "Engine " + engineId + " has finished discovering tests");
		}
	}

	@Override
	public void selectorProcessed(UniqueId engineId, DiscoverySelector selector, SelectorResolutionResult result) {
		switch (result.getStatus()) {
			case RESOLVED:
				logger.debug(() -> selector + " was resolved by " + engineId);
				break;
			case FAILED:
				logger.error(result.getThrowable(), () -> "Resolution of " + selector + " by " + engineId + " failed");
				break;
			case UNRESOLVED:
				Consumer<Supplier<String>> loggingConsumer = logger::debug;
				if (selector instanceof UniqueIdSelector) {
					UniqueId uniqueId = ((UniqueIdSelector) selector).getUniqueId();
					if (uniqueId.hasPrefix(engineId)) {
						loggingConsumer = logger::warn;
					}
				}
				loggingConsumer.accept(() -> selector + " could not be resolved by " + engineId);
				break;
		}
	}

}
