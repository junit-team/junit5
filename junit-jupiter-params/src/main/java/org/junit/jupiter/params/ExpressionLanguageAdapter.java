package org.junit.jupiter.params;

import java.util.Map;

public interface ExpressionLanguageAdapter {

	String evaluate(String template, Map<String, Object> context);
}
