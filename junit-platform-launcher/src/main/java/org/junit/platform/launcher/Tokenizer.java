package org.junit.platform.launcher;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tokenizer {

    static class IllegalTagExpression extends RuntimeException {
    }

    private final List<Pattern> patterns = new LinkedList<>();
    private final List<String> operators = new ArrayList<>();

    public Tokenizer() {
        patterns.add(Pattern.compile("^(\\()"));
        patterns.add(Pattern.compile("(\\))"));
        patterns.add(Pattern.compile("^(not)\\s"));
        patterns.add(Pattern.compile("\\s(and|or)\\s"));
        patterns.add(Pattern.compile("^([a-zA-Z 0-9_]+)"));

        operators.add("(");
        operators.add(")");
        operators.add("not");
        operators.add("or");
        operators.add("and");

    }

    public List<String> tokenizeWithPostProcessing(String infixTagExpression) {

        List<String> tokens = new LinkedList<>();

        String[] parts = infixTagExpression.replaceAll("\\(", " ( ").replaceAll("\\)", " ) ").split("\\s");

        for (int i = 0; i < parts.length; i++) {
            String currentPart = parts[i];
            if (isPartOfATag(currentPart)) {
                for (int j = i + 1; j < parts.length; j++) {
                    String nextPart = parts[j];
                    if (isPartOfATag(nextPart)) {
                        currentPart = currentPart + " " + nextPart;
                        ++i;
                    } else {
                        break;
                    }
                }
            }

            String tokenCandidate = currentPart.trim();
            if (!tokenCandidate.isEmpty()) {
                tokens.add(tokenCandidate);
            }
        }

        return tokens;
    }

    private boolean isPartOfATag(String currentPart) {
        return !operators.contains(currentPart);
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
                    if (pattern.pattern().contains("and") || pattern.pattern().contains(")")) {
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
