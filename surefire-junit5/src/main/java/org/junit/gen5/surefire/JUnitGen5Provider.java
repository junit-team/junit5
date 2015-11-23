
package org.junit.gen5.surefire;

import java.lang.reflect.InvocationTargetException;

import org.apache.maven.surefire.providerapi.AbstractProvider;
import org.apache.maven.surefire.providerapi.ProviderParameters;
import org.apache.maven.surefire.report.ReporterException;
import org.apache.maven.surefire.suite.RunResult;
import org.apache.maven.surefire.testset.TestSetFailedException;

public class JUnitGen5Provider extends AbstractProvider {

	public JUnitGen5Provider(ProviderParameters booterParameters) {
		// TODO Implement this.
	}

	@Override
	public Iterable<Class<?>> getSuites() {
		// TODO Implement this.
		return null;
	}

	@Override
	public RunResult invoke(Object forkTestSet)
			throws TestSetFailedException, ReporterException, InvocationTargetException {
		// TODO Implement this.
		throw new TestSetFailedException("Hello from JUnit5!");
	}

}
