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

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Stream;

import org.junit.platform.commons.util.ClassLoaderUtils;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.DiscoverySelector;

class SelectorParsers implements SelectorParserContext {

	private final Map<String, SelectorParser> parsers = loadParsers();

	private static Map<String, SelectorParser> loadParsers() {
		Map<String, SelectorParser> parsers = new HashMap<>();
		Iterable<SelectorParser> listeners = ServiceLoader.load(SelectorParser.class,
			ClassLoaderUtils.getDefaultClassLoader());
		for (SelectorParser parser : listeners) {
			SelectorParser previous = parsers.put(parser.getPrefix(), parser);
			Preconditions.condition(previous == null,
				() -> String.format("Duplicate parser for prefix: [%s] candidate a: [%s] candidate b: [%s] ",
					parser.getPrefix(), previous.getClass().getName(), parser.getClass().getName()));

		}
		return parsers;
	}

	@Override
	public Stream<DiscoverySelector> parse(String selector) {
		TBD uri = TBD.parse(selector);
		String scheme = uri.getPrefix();
		Preconditions.notNull(scheme, "Selector must have a scheme: " + selector);

		SelectorParser parser = parsers.get(scheme);
		Preconditions.notNull(parser, "No parser for scheme: " + scheme);

		return parser.parse(uri, this);
	}

}
