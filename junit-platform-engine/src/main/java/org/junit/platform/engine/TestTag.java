/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.engine;

import static org.junit.platform.commons.meta.API.Usage.Experimental;

import java.io.Serializable;
import java.util.Objects;

import org.junit.platform.commons.meta.API;
import org.junit.platform.commons.util.PreconditionViolationException;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.StringUtils;

/**
 * Immutable value object for a <em>tag</em> that is assigned to a test or
 * container.
 *
 * @since 1.0
 * @see #isValid(String)
 * @see #create(String)
 */
@API(Experimental)
public final class TestTag implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String name;

	/**
	 * Determine if the supplied tag name is valid with regard to the supported
	 * syntax for tags.
	 *
	 * <h3>Syntax Rules for Tags</h3>
	 * <ul>
	 * <li>A tag must not be {@code null}.</li>
	 * <li>A tag must not be blank.</li>
	 * <li>A trimmed tag must not contain whitespace.</li>
	 * <li>A trimmed tag must not contain ISO control characters.</li>
	 * </ul>
	 *
	 * <p>If this method returns {@code true} for a given name, it is then a
	 * valid candidate for the {@link TestTag#create(String) create()} factory
	 * method.
	 *
	 * @param name the name of the tag to validate; may be {@code null} or blank
	 * @return {@code true} if the supplied tag name conforms to the supported
	 * syntax for tags
	 * @see StringUtils#isNotBlank(String)
	 * @see String#trim()
	 * @see StringUtils#doesNotContainWhitespace(String)
	 * @see StringUtils#doesNotContainIsoControlCharacter(String)
	 * @see TestTag#create(String)
	 */
	public static boolean isValid(String name) {
		if (name == null) {
			return false;
		}
		name = name.trim();

		return !name.isEmpty() && //
				StringUtils.doesNotContainWhitespace(name) && //
				StringUtils.doesNotContainIsoControlCharacter(name);
	}

	/**
	 * Create a {@code TestTag} from the supplied {@code name}.
	 *
	 * <p>Consider checking whether the syntax of the supplied {@code name}
	 * is {@linkplain #isValid(String) valid} before attempting to create a
	 * {@code TestTag} using this factory method.
	 *
	 * <p>Note: the supplied {@code name} will be {@linkplain String#trim() trimmed}.
	 *
	 * @param name the name of the tag; must be syntactically <em>valid</em>
	 * @throws PreconditionViolationException if the supplied tag name is not
	 * syntactically <em>valid</em>
	 * @see TestTag#isValid(String)
	 */
	public static TestTag create(String name) throws PreconditionViolationException {
		return new TestTag(name);
	}

	private TestTag(String name) {
		Preconditions.condition(TestTag.isValid(name),
			() -> String.format("Tag name [%s] must be syntactically valid", name));
		this.name = name.trim();
	}

	/**
	 * Get the name of this tag.
	 *
	 * @return the name of this tag; never {@code null} or blank
	 */
	public String getName() {
		return this.name;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TestTag) {
			TestTag that = (TestTag) obj;
			return Objects.equals(this.name, that.name);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.name.hashCode();
	}

	@Override
	public String toString() {
		return this.name;
	}

}
