package org.junit.platform.engine.discovery;

import org.junit.platform.engine.DiscoverySelector;

import java.net.URI;
import java.util.stream.Stream;

public interface SelectorParser {
    String getPrefix();

    Stream<DiscoverySelector> parse(URI selector);
}
