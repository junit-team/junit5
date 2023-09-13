package org.junit.platform.engine.discovery;

import org.junit.platform.engine.DiscoverySelector;

import java.util.stream.Stream;

public interface SelectorParserContext {
    Stream<DiscoverySelector> parse(String selector);
}
