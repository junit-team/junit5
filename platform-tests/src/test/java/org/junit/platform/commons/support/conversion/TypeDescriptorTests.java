package org.junit.platform.commons.support.conversion;

import static org.junit.jupiter.api.EqualsAndHashCodeAssertions.assertEqualsAndHashCode;

import org.junit.jupiter.api.Test;

class TypeDescriptorTests {

	@Test
	void equalsAndHashCode() {
		var typeDescriptor1 = TypeDescriptor.forClass(String.class);
		var typeDescriptor2 = TypeDescriptor.forClass(String.class);
		var typeDescriptor3 = TypeDescriptor.forClass(Object.class);

		assertEqualsAndHashCode(typeDescriptor1, typeDescriptor2, typeDescriptor3);
	}

}
