
package org.junit.jupiter.params;

public interface ExpressionLanguageAdapter {

	void compile(String template);

	void format(ArgumentsContext argumentsContext, StringBuffer stringBuffer);
}
