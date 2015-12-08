
package org.junit.gen5.engine.junit5;

@FunctionalInterface
public interface TestInstanceProvider {

	Object getTestInstance() throws Throwable;

}
