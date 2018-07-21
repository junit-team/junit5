/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.support.descriptor;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.Serializable;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link ClassSource}.
 *
 * @since 1.0
 */
class ClassSourceTests extends AbstractTestSourceTests {

	@Override
	Stream<Serializable> createSerializableInstances() throws Exception {
		return Stream.of( //
			ClassSource.from("class.source"), //
			ClassSource.from("class.and.position", FilePosition.from(1, 2)) //
		);
	}

	@Test
	void classNameSource() {
		String testClassName = "com.unknown.mypackage.ClassByName";
		ClassSource source = ClassSource.from(testClassName);

		assertThat(source.getClassName()).isEqualTo(testClassName);
		assertThat(source.getPosition()).isEmpty();
	}

	@Test
	void classNameSourceWithFilePosition() {
		String testClassName = "com.unknown.mypackage.ClassByName";
		FilePosition position = FilePosition.from(42, 23);
		ClassSource source = ClassSource.from(testClassName, position);

		assertThat(source.getClassName()).isEqualTo(testClassName);
		assertThat(source.getPosition()).isNotEmpty();
		assertThat(source.getPosition()).hasValue(position);
	}

	@Test
	void classSource() {
		Class<?> testClass = getClass();
		ClassSource source = ClassSource.from(testClass);

		assertThat(source.getJavaClass()).isEqualTo(testClass);
		assertThat(source.getPosition()).isEmpty();
	}

	@Test
	void classSourceWithFilePosition() {
		Class<?> testClass = getClass();
		FilePosition position = FilePosition.from(42, 23);
		ClassSource source = ClassSource.from(testClass, position);

		assertThat(source.getJavaClass()).isEqualTo(testClass);
		assertThat(source.getPosition()).isNotEmpty();
		assertThat(source.getPosition()).hasValue(position);
	}

	@Test
	void equalsAndHashCodeForClassSource() {
		Class<?> class1 = String.class;
		Class<?> class2 = Number.class;
		assertEqualsAndHashCode(ClassSource.from(class1), ClassSource.from(class1), ClassSource.from(class2));
	}

}
