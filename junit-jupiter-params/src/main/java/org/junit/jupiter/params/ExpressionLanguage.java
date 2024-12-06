package org.junit.jupiter.params;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ExpressionLanguage {

	Class<? extends ExpressionLanguageAdapter> value();
}
