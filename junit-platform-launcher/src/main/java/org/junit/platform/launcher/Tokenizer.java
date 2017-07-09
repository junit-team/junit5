package org.junit.platform.launcher;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Tokenizer {

    static class IllegalTagExpression extends RuntimeException {
    }

    private final List<Pattern> patterns = new LinkedList<>();

    Tokenizer() {
        patterns.add(Pattern.compile("^(\\()"));
        patterns.add(Pattern.compile("^(\\))"));
        patterns.add(Pattern.compile("^(not)\\s"));
        patterns.add(Pattern.compile("\\s(and|or)\\s"));
        patterns.add(Pattern.compile("^([a-zA-Z 0-9_]+)"));
    }

    List<String> tokenize(String infixTagExpression) {
        LinkedList<String> tokens = new LinkedList<>();
        String toParse = infixTagExpression;

        while (!toParse.isEmpty()) {
            toParse = toParse.trim();
            boolean patternsExhausted = true;
            for (Pattern pattern : patterns) {
                Matcher matcher = pattern.matcher(toParse);
                if (matcher.find()) {
                    if (pattern.pattern().contains("and")) {
                        String leftPart = toParse.substring(0, matcher.start()).trim();
                        tokens.addAll(tokenize(leftPart));
                    }
                    tokens.add(matcher.group(1).trim());
                    toParse = toParse.substring(matcher.end());
                    patternsExhausted = false;
                    break;
                }
            }
            if (patternsExhausted) {
                throw new IllegalTagExpression();
            }
        }
        return tokens;
    }
}
