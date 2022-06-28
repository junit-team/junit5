/*
 * Copyright 2015-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine;

/*
 * Copyright 2015-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Integration tests that explicitly demonstrate the shadowing/overriding
 * rules for lifecylce methods in the {@link JupiterTestEngine}.
 *
 * @since 5.9
 */
class LifecycleMethodShadowingTests {

	@Nested
	@DisplayName("A package-private lifecycle super-method can be combined with")
	class PackagePrivateSuperClassTests {

		@Nested
		@DisplayName("a protected lifecycle method in the derived class")
		class ProtectedExtendsPackagePrivateLifecycleMethod
				extends SuperClassWithPackagePrivateLifecycleMethodTestCase {

			@BeforeEach
			protected void beforeEach() {
			}

		}

		@Nested
		@DisplayName("a package-private lifecycle method in the derived class")
		class PackagePrivateExtendsPackagePrivateLifecycleMethod
				extends SuperClassWithPackagePrivateLifecycleMethodTestCase {

			@BeforeEach
			void beforeEach() {
			}

		}

		@Nested
		@DisplayName("a public lifecycle method in the derived class")
		class PublicExtendsPackagePrivateLifecycleMethod extends SuperClassWithPackagePrivateLifecycleMethodTestCase {

			@BeforeEach
			public void beforeEach() {
			}

		}
	}

	@Nested
	@DisplayName("A protected lifecycle super-method can be combined with")
	class ProtectedSuperClassTests {

		@Nested
		@DisplayName("a protected lifecycle method in the derived class")
		class ProtectedExtendsPackagePrivate extends SuperClassWithProtectedLifecycleMethodTestCase {

			@BeforeEach
			protected void beforeEach() {
			}

		}

		@Nested
		@DisplayName("a public lifecycle method in the derived class")
		class PublicExtendsPackagePrivate extends SuperClassWithProtectedLifecycleMethodTestCase {

			@BeforeEach
			public void beforeEach() {
			}

		}
	}

	@Nested
	@DisplayName("A public lifecycle super-method can be combined with")
	class PublicSuperClassTests {

		@Nested
		@DisplayName("a public lifecycle method in the derived class")
		class PublicExtendsPackagePrivate extends SuperClassWithPublicLifecycleMethodTestCase {

			@BeforeEach
			public void beforeEach() {
			}

		}
	}

}

// -------------------------------------------------------------------------

class SuperClassWithPackagePrivateLifecycleMethodTestCase {

	@BeforeEach
	void beforeEach() {
		fail();
	}

	@Test
	void test() {
	}

}

class SuperClassWithProtectedLifecycleMethodTestCase {

	@BeforeEach
	protected void beforeEach() {
		fail();
	}

	@Test
	void test() {
	}

}

class SuperClassWithPublicLifecycleMethodTestCase {

	@BeforeEach
	public void beforeEach() {
		fail();
	}

	@Test
	void test() {
	}

}
