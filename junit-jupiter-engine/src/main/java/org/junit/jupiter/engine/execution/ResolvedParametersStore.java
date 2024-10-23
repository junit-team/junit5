package org.junit.jupiter.engine.execution;

/**
 * Internal interface to be implemented by extension contexts that store resolved parameters.
 */
public interface ResolvedParametersStore {
	void recordResolvedParameter(Object value);
}
