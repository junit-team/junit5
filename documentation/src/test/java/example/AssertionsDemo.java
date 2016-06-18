/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package example;

// @formatter:off
// tag::user_guide[]
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.expectThrows;

import org.junit.jupiter.api.Test;

class AssertionsDemo {

	// end::user_guide[]
	Address address = new Address("John", "User");

	// tag::user_guide[]
	@Test
	void standardAssertions() {
		assertEquals(2, 2);
		assertEquals(4, 4, "The optional assertion message is now the last parameter.");
		assertTrue(2 == 2, () -> "Assertion messages can be lazily evaluated -- "
				+ "to avoid constructing complex messages unnecessarily.");
	}

	@Test
	void groupedAssertions() {
		// In a grouped assertion all assertions are executed, and any
		// failures will be reported together.
		assertAll("address",
			() -> assertEquals("John", address.getFirstName()),
			() -> assertEquals("User", address.getLastName())
		);
	}

	@Test
	void exceptionTesting() {
		Throwable exception = expectThrows(IllegalArgumentException.class, () -> {
			throw new IllegalArgumentException("a message");
		});
		assertEquals("a message", exception.getMessage());
	}

}
// end::user_guide[]
// @formatter:on

class Address {

	private final String firstName;

	public String getLastName() {
		return lastName;
	}

	private final String lastName;

	public String getFirstName() {
		return firstName;
	}

	public Address(String firstName, String lastName) {

		this.firstName = firstName;
		this.lastName = lastName;
	}
}
