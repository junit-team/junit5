package org.junit.lambda.prototype01;

import java.util.Optional;

public interface TestExecutionContext {
	
	Optional<TestExecutionContext> getParent();

}
