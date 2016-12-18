package example.testinterface;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

//tag::user_guide[]
public class TestInterfaceDemoTests implements TestConsoleLogger, TimeExecutionLogger, DynamicTests {

	@Test
	void isEqualValue(TestInfo testInfo) {
		assertEquals(1, 1, "is always equal");
	}

}
//end::user_guide[]
