package example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

@Execution(ExecutionMode.CONCURRENT)
class ExplicitExecutionModeDemo {

	@Test
	void testA() {
		// concurrent
	}

	@Test
	@Execution(ExecutionMode.SAME_THREAD)
	void testB() {
		// overrides to same_thread
	}
}