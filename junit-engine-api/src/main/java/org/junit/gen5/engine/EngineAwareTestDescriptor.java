
package org.junit.gen5.engine;

public interface EngineAwareTestDescriptor extends TestDescriptor {

	TestEngine getEngine();

}
