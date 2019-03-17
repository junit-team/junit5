/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.descriptor;

import static org.apiguardian.api.API.Status.INTERNAL;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apiguardian.api.API;
import org.junit.jupiter.api.TestReporter;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;

/**
 * {@link TestDescriptor} for reporting checkpoints.
 *
 * @since 5.5
 */
@API(status = INTERNAL, since = "5.5")
class CheckpointTestDescriptor extends AbstractTestDescriptor {

	private static final AtomicInteger CHECKPOINT_COUNTER = new AtomicInteger();

	static String getCheckpointValue(Map<String, String> values) {
		return values.get(TestReporter.CHECKPOINT_ENTRY_KEY);
	}

	static CheckpointTestDescriptor of(TestDescriptor testDescriptor, String checkpointDisplayName) {
		String idValue = "#" + CHECKPOINT_COUNTER.getAndIncrement();
		UniqueId checkpointId = testDescriptor.getUniqueId().append("checkpoint", idValue);
		TestSource checkpointSource = testDescriptor.getSource().orElse(null);
		return new CheckpointTestDescriptor(checkpointId, checkpointDisplayName, checkpointSource);
	}

	private CheckpointTestDescriptor(UniqueId uniqueId, String displayName, TestSource source) {
		super(uniqueId, displayName, source);
	}

	@Override
	public Type getType() {
		return Type.TEST;
	}
}
