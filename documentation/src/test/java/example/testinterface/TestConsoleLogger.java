package example.testinterface;


import java.util.logging.Logger;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;

//tag::user_guide[]
public interface TestConsoleLogger {
	
	public static final Logger LOG = Logger.getLogger(TestConsoleLogger.class.getName());
	
	@BeforeAll
	static void beforeAllTest(TestInfo info) {
		LOG.info(() -> "beforeAllTest");
	}
	
	@AfterAll
	static void afterAllTest(TestInfo info) {
		LOG.info(() -> "afterAllTest");
	}

	@BeforeEach
	default void beforeEachTest(TestInfo testInfo) {
		LOG.info(() -> String.format("About to execute [%s]", testInfo.getTestMethod().get().getName()));
	}
	
	@AfterEach
	default void afterEachTest(TestInfo testInfo) {
		LOG.info(() -> String.format("Finished executing [%s]", testInfo.getTestMethod().get().getName()));
	}

}
//end::user_guide[]
