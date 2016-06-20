/*
 * Copyright 2015-2016 the original author or authors.
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
 * Annotates public types within JUnit to indicate their level of stability
 * and how they are intended to be used.
 *
 * <p>If the annotation is present on a type, it is considered to hold for
 * all public members of the type as well. A member is allowed to declare a
 * different {@link Usage} of lower stability.
 *
 * @since 1.0
 */
@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR })
@Retention(RetentionPolicy.CLASS)
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
		 * Should no longer be used, might disappear in the next minor release.
		 */
		Deprecated,

		/**
		 * Intended for new, experimental features where we are looking for feedback.
		 *
		 * <p>Use with caution, might be promoted to {@code Maintained} or
		 * {@code Stable} in the future, but might also be removed without
		 * prior notice.
		 */
		Experimental,

		/**
		 * Intended for features that will not be changed in a backwards-
		 * incompatible way for at least the next minor release of the current
		 * major version. If scheduled for removal, it will be demoted to
		 * {@code Deprecated} first.
		 */
		Maintained,

		/**
		 * Intended for features that will not be changed in a backwards-
		 * incompatible way in the current major version.
		 */
		Stable

	}

}
