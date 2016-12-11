/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.engine;

import static org.junit.platform.commons.meta.API.Usage.Experimental;

import org.junit.platform.commons.meta.API;
import org.junit.platform.commons.util.PackageUtils;

/**
 * A {@code TestEngine} facilitates <em>discovery</em> and <em>execution</em> of
 * tests for a particular programming model.
 *
 * <p>For example, JUnit provides a {@code TestEngine} that discovers and
 * executes tests written using the JUnit Jupiter programming model.
 *
 * <p>Every {@code TestEngine} must {@linkplain #getId provide its own unique ID},
 * {@linkplain #discover discover tests} from
 * {@link EngineDiscoveryRequest EngineDiscoveryRequests},
 * and {@linkplain #execute execute those tests} according to
 * {@link ExecutionRequest ExecutionRequests}.
 *
 * <p>In order to facilitate test discovery within IDEs and tools prior
 * to launching the JUnit Platform, {@code TestEngine} implementations are
 * encouraged to make use of the
 * {@link org.junit.platform.commons.annotation.Testable @Testable} annotation.
 * For example, the {@code @Test} and {@code @TestFactory} annotations in JUnit
 * Jupiter are meta-annotated with {@code @Testable}. Consult the Javadoc for
 * {@code @Testable} for further details.
 *
 * @see org.junit.platform.engine.EngineDiscoveryRequest
 * @see org.junit.platform.engine.ExecutionRequest
 * @see org.junit.platform.commons.annotation.Testable
 * @since 1.0
 */
@API(Experimental)
public interface TestEngine {

	/**
	 * Get the ID that uniquely identifies this test engine.
	 *
	 * <p>Each test engine must provide a unique ID. For example, JUnit Vintage
	 * and JUnit Jupiter use {@code "junit-vintage"} and {@code "junit-jupiter"},
	 * respectively. When in doubt, you may use the fully qualified name of your
	 * custom {@code TestEngine} implementation class.
	 */
	String getId();

	/**
	 * Get the implementation version of this test engine.
	 *
	 * <p>This default implementation tries to query the implementation version
	 * from the package attributes. Packages have attributes only if the
	 * information was defined in the manifests that accompany the classes, and
	 * if the class loader created the package instance with the attributes
	 * from the manifest.
	 *
	 * <p>If the implementation version can not be queried from the package
	 * attributes, this method returns {@code "DEVELOPMENT"}.
	 *
	 * <p>It is up to the engine implementation to override this behavior with
	 * a potentially constant version string of any kind.
	 *
	 * @return implementation version or {@code "DEVELOPMENT"}
	 * @see Class#getPackage()
	 * @see Package#getImplementationVersion()
	 */
	default String getVersion() {
		return PackageUtils.getAttribute(getClass(), Package::getImplementationVersion).orElse("DEVELOPMENT");
	}

	/**
	 * Discover tests according to the supplied {@link EngineDiscoveryRequest}.
	 *
	 * <p>The supplied {@link UniqueId} must be used as the unique ID of the
	 * returned root {@link TestDescriptor}. In addition, the {@code UniqueId}
	 * must be used to create unique IDs for children of the root's descriptor
	 * by calling {@link UniqueId#append}.
	 *
	 * @param discoveryRequest the discovery request
	 * @param uniqueId the unique ID to be used for this test engine's
	 * {@code TestDescriptor}
	 * @return the root {@code TestDescriptor} of this engine, typically an
	 * instance of {@code EngineDescriptor}
	 * @see org.junit.platform.engine.support.descriptor.EngineDescriptor
	 */
	TestDescriptor discover(EngineDiscoveryRequest discoveryRequest, UniqueId uniqueId);

	/**
	 * Execute tests according to the supplied {@link ExecutionRequest}.
	 *
	 * <p>The {@code request} passed to this method contains the root
	 * {@link TestDescriptor} that was previously returned by {@link #discover},
	 * the {@link EngineExecutionListener} to be notified of test execution
	 * events, and {@link ConfigurationParameters} that may influence test execution.
	 *
	 * @param request the request to execute tests for
	 */
	void execute(ExecutionRequest request);

}
