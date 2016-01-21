/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.junit.gen5.commons.JUnitException;
import org.junit.gen5.engine.UniqueId.Segment;

class UniqueIdParser {
	private final String uniqueIdString;
	private final String segmentDelimiter;
	private final Pattern segmentPattern;

	public UniqueIdParser(String uniqueIdString, String segmentDelimiter, String typeValueSeparator) {
		this.uniqueIdString = uniqueIdString;
		this.segmentDelimiter = segmentDelimiter;
		this.segmentPattern = Pattern.compile("\\[(.+)\\" + typeValueSeparator + "(.+)\\]");
	}

	UniqueId parse() {
		String[] parts = uniqueIdString.split(segmentDelimiter);
		List<Segment> segments = Arrays.stream(parts).map(this::createSegment).collect(Collectors.toList());
		return new UniqueId(segments);
	}

	private Segment createSegment(String segmentString) {
		Matcher segmentMatcher = segmentPattern.matcher(segmentString);
		if (!segmentMatcher.matches())
			throw new JUnitException(String.format("'%s' is not a well-formed UniqueId", uniqueIdString));
		String type = segmentMatcher.group(1);
		String value = segmentMatcher.group(2);
		return new Segment(type, value);
	}
}
