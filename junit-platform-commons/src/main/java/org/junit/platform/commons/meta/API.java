/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.commons.meta;

import static org.junit.platform.commons.meta.API.Status.INTERNAL;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@code @API} is used to annotate public types, methods, constructors, and
 * fields within a framework or application in order to indicate their level of
 * stability and how they are intended to be used by consumers of the API.
 *
 * <p>If {@code @API} is present on a type, it is considered to hold for
 * all public members of the type as well. However, a member of such an
 * annotated type is allowed to declare a different {@link Status} of lower
 * stability.
 *
 * @since 1.0
 */
@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@API(status = INTERNAL, since = "1.0")
public @interface API {

	/**
	 * The current {@linkplain Status status} of the API.
	 */
	Status status();

	/**
	 * The version of the API when the {@link #status} was last changed.
	 *
	 * <p>Defaults to an empty string, signifying that the <em>since</em>
	 * version is unknown.
	 */
	String since() default "";

	/**
	 * List of packages belonging to intended consumers.
	 *
	 * <p>The supplied packages can be fully qualified package names or
	 * patterns containing asterisks that will be used as wildcards.
	 *
	 * <p>Defaults to {@code "*"}, signifying that the API is intended to be
	 * consumed by any package.
	 */
	String[] consumers() default "*";

	/**
	 * Indicates the status of an API element and therefore its level of
	 * stability as well.
	 */
	enum Status {

		/**
		 * Must not be used by any external code. Might be removed without prior
		 * notice.
		 */
		INTERNAL,

		/**
		 * Should no longer be used. Might disappear in the next minor release.
		 */
		DEPRECATED,

		/**
		 * Intended for new, experimental features where the publisher of the
		 * API is looking for feedback.
		 *
		 * <p>Use with caution. Might be promoted to {@link #MAINTAINED} or
		 * {@link #STABLE} in the future, but might also be removed without
		 * prior notice.
		 */
		EXPERIMENTAL,

		/**
		 * Intended for features that will not be changed in a backwards-
		 * incompatible way for at least the next minor release of the current
		 * major version. If scheduled for removal, such a feature will be
		 * demoted to {@link #DEPRECATED} first.
		 */
		MAINTAINED,

		/**
		 * Intended for features that will not be changed in a backwards-
		 * incompatible way in the current major version.
		 */
		STABLE;

	}

}
