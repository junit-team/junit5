package org.junit.gen5.api;

/**
 * @author Sam Brannen
 * @since 5.0
 */
@FunctionalInterface
public interface Executable {

	void execute() throws Throwable;

}
