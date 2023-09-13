package org.junit.platform.engine.discovery;

import org.junit.platform.commons.util.ClassLoaderUtils;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.DiscoverySelector;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Stream;

class SelectorParsers  implements SelectorParserContext{

    private final Map<String, SelectorParser> parsers = loadParsers();

    private static Map<String, SelectorParser> loadParsers() {
        Map<String, SelectorParser> parsers = new HashMap<>();
        Iterable<SelectorParser> listeners = ServiceLoader.load(SelectorParser.class, ClassLoaderUtils.getDefaultClassLoader());
        for (SelectorParser parser : listeners) {
            SelectorParser previous = parsers.put(parser.getPrefix(), parser);
            Preconditions.condition(previous == null,
                    () -> String.format("Duplicate parser for prefix: [%s] candidate a: [%s] candidate b: [%s] ",
                            parser.getPrefix(),
                            previous.getClass().getName(),
                            parser.getClass().getName()
                    ));

        }
        return parsers;
    }

    @Override
    public Stream<DiscoverySelector> parse(String selector) {
        URI uri = URI.create(selector);
        String scheme = uri.getScheme();
        Preconditions.notNull(scheme, "Selector must have a scheme: " + selector);

        SelectorParser parser = parsers.get(scheme);
        Preconditions.notNull(parser, "No parser for scheme: " + scheme);

        return parser.parse(uri, this);
    }

}
