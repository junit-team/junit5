
package org.junit.gen5.launcher;

import java.util.ServiceLoader;

import org.junit.gen5.engine.TestEngine;

/**
 * @author Stefan Bechtold
 * @since 5.0
 */
class TestEngineRegistry {

	private static Iterable<TestEngine> testEngines;


	static Iterable<TestEngine> lookupAllTestEngines() {
		if (testEngines == null) {
			testEngines = ServiceLoader.load(TestEngine.class);
		}
		return testEngines;
	}

}
