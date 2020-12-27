/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.suite.engine;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

import java.util.List;
import java.util.Optional;

import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.UniqueId.Segment;

final class UniqueIdHelper {

	static UniqueId append(UniqueId uniqueId, UniqueId suffix) {
		Preconditions.notNull(uniqueId, "uniqueId most not be null");
		Preconditions.notNull(suffix, "suffix most not be null");

		UniqueId accumulator = uniqueId;
		for (Segment segment : suffix.getSegments()) {
			accumulator = accumulator.append(segment);
		}
		return accumulator;
	}

	static Optional<UniqueId> uniqueIdOfSegment(UniqueId uniqueId, String segmentType) {
		Preconditions.notNull(uniqueId, "uniqueId most not be null");
		Preconditions.notNull(segmentType, "segmentType most not be null");

		List<Segment> segments = uniqueId.getSegments();
		return segments.stream().filter(segment -> segment.getType().equals(segmentType)).findFirst().map(
			segments::indexOf).map(index -> segments.subList(0, index + 1)).map(UniqueIdHelper::fromSegments);
	}

	private static UniqueId fromSegments(List<Segment> subSegments) {
		Segment engineSegment = subSegments.get(0);
		UniqueId accumulator = UniqueId.root(engineSegment.getType(), engineSegment.getValue());
		for (int i = 1; i < subSegments.size(); i++) {
			accumulator = accumulator.append(subSegments.get(i));
		}
		return accumulator;
	}

	static Optional<UniqueId> removePrefix(UniqueId uniqueId, UniqueId prefix) {
		Preconditions.notNull(uniqueId, "uniqueId most not be null");
		Preconditions.notNull(prefix, "prefix most not be null");
		Preconditions.condition(uniqueId.hasPrefix(prefix), "prefix must be a prefix of uniqueId");

		if (uniqueId.equals(prefix)) {
			return Optional.empty();
		}

		List<Segment> uniqueIdSegments = uniqueId.getSegments();
		List<Segment> subSegments = uniqueIdSegments.subList(prefix.getSegments().size(), uniqueIdSegments.size());
		return Optional.of(fromSegments(subSegments));
	}

	static boolean containCycle(UniqueId uniqueId, String segmentType) {
		Preconditions.notNull(uniqueId, "uniqueId most not be null");
		Preconditions.notNull(segmentType, "segmentType most not be null");

		// @formatter:off
		return uniqueId.getSegments().stream()
				.filter(segment -> segmentType.equals(segment.getType()))
				.map(Segment::getValue)
				.collect(groupingBy(identity(), counting()))
				.values()
				.stream()
				.anyMatch(count -> count > 1);
		// @formatter:on
	}

}
