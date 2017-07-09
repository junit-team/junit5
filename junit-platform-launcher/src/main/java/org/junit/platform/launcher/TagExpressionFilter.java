package org.junit.platform.launcher;

import io.cucumber.tagexpressions.Expression;
import io.cucumber.tagexpressions.TagExpressionParser;
import org.junit.platform.engine.FilterResult;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestTag;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class TagExpressionFilter {
    public static PostDiscoveryFilter parse(String tagExpression) {
        Expression expression = new TagExpressionParser().parse(tagExpression);
        return descriptor -> FilterResult.includedIf(expression.evaluate(trimmedTagsOf(descriptor)));
    }

    private static List<String> trimmedTagsOf(TestDescriptor descriptor) {
        // @formatter:off
        return descriptor.getTags().stream()
                .map(TestTag::getName)
                .map(String::trim)
                .collect(toList());
        // @formatter:on
    }
}
