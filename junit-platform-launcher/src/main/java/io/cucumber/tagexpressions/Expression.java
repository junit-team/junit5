package io.cucumber.tagexpressions;

import java.util.List;

public interface Expression {
    boolean evaluate(List<String> variables);
}
