/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.commons.meta;

import static org.junit.platform.commons.meta.API.Usage.Internal;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@code @API} is used to annotate public types, methods, constructors, and
 * fields within JUnit in order to indicate their level of stability and how
 * they are intended to be used.
 *
 * <p>If {@code @API} is present on a type, it is considered to hold for
 * all public members of the type as well. However, a member of such an
 * annotated type is allowed to declare a different {@link Usage} of lower
 * stability.
 *
 * @since 1.0
 */
@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@API(Internal)
public @interface API {

	Usage value();

	/**
	 * Indicates the level of stability of an API element.
	 */
	enum Usage {

		/**
		 * Must not be used by any code other than JUnit itself. Might be
		 * removed without prior notice.
		 */
		Internal,

		/**
		 * Should no longer be used. Might disappear in the next minor release.
		 */
		Deprecated,

		/**
		 * Intended for new, experimental features where the JUnit team is
		 * looking for feedback.
		 *
		 * <p>Use with caution. Might be promoted to {@link #Maintained} or
		 * {@link #Stable} in the future, but might also be removed without
		 * prior notice.
		 */
		Experimental,

		/**
		 * Intended for features that will not be changed in a backwards-
		 * incompatible way for at least the next minor release of the current
		 * major version. If scheduled for removal, such a feature will be
		 * demoted to {@link #Deprecated} first.
		 */
		Maintained,

		/**
		 * Intended for features that will not be changed in a backwards-
		 * incompatible way in the current major version.
		 */
		Stable;

	}

}
