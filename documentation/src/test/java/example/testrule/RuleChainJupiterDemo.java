package example.testrule;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

public class RuleChainJupiterDemo {

	@Test
	@ExtendWith(OuterLoggingExtension.class)
	@ExtendWith(MiddleLoggingExtension.class)
	@ExtendWith(InnerLoggingExtension.class)
	public void example1() {
		assertTrue(true);
	}
	
	@Test
	@ExtendWith({
		OuterLoggingExtension.class,
		MiddleLoggingExtension.class,
		InnerLoggingExtension.class
	})
	public void example2() {
		assertTrue(true);
	}
	
}
