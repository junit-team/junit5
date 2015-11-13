package org.junit.gen5.engine;

public interface ClassFilter extends EngineFilter {

	boolean acceptClass(Class<?> clazz);
}
