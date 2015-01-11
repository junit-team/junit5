package org.junit.lambda.proposal01;

import org.junit.rules.TestRule;

/**
 * See test source folder!
 */
public class JUnitTest {
	
	interface Block {
		
		void execute() throws Exception;
		
	}

	protected void test(String testName, Block block) {
		// TODO implement me :-)
	}

	protected void beforeAll(Block block) {
		// TODO implement me :-)
	}

	protected void beforeEach(Block block) {
		// TODO implement me :-)
	}

	protected void afterEach(Block block) {
		// TODO implement me :-)
	}

	protected void afterAll(Block block) {
		// TODO implement me :-)
	}

	protected <T extends TestRule> T aroundEach(T rule) {
		return rule;
	}

	protected <T extends TestRule> T aroundAll(T rule) {
		return rule;
	}

}
