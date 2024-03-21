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

import org.junit.platform.commons.util.ClassLoaderUtils;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.DiscoverySelectorIdentifier;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Stream;

class SelectorParsers implements DiscoverySelectorIdentifierParser.Context {

	private final Map<String, DiscoverySelectorIdentifierParser> parsers = loadParsers();

	private static Map<String, DiscoverySelectorIdentifierParser> loadParsers() {
		Map<String, DiscoverySelectorIdentifierParser> parsers = new HashMap<>();
		Iterable<DiscoverySelectorIdentifierParser> listeners = ServiceLoader.load(DiscoverySelectorIdentifierParser.class,
			ClassLoaderUtils.getDefaultClassLoader());
		for (DiscoverySelectorIdentifierParser parser : listeners) {
			DiscoverySelectorIdentifierParser previous = parsers.put(parser.getPrefix(), parser);
			Preconditions.condition(previous == null,
				() -> String.format("Duplicate parser for prefix: [%s] candidate a: [%s] candidate b: [%s] ",
					parser.getPrefix(), previous.getClass().getName(), parser.getClass().getName()));

		}
		return parsers;
	}

	@Override
	public Stream<DiscoverySelector> parse(String selector) {
		DiscoverySelectorIdentifier identifier = DiscoverySelectorIdentifier.parse(selector);

        DiscoverySelectorIdentifierParser parser = parsers.get(identifier.getPrefix());
		Preconditions.notNull(parser, "No parser for prefix: " + identifier.getPrefix());

		return parser.parse(identifier, this);
	}

}
