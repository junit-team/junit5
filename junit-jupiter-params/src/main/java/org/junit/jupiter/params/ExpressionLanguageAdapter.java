
package org.junit.jupiter.params;

public interface ExpressionLanguageAdapter {

	void compile(String template);

	void format(Object scope, StringBuffer result);
}
