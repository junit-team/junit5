/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.commons.support;

import static org.apiguardian.api.API.Status.MAINTAINED;

import java.lang.reflect.Member;

import org.apiguardian.api.API;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ReflectionUtils;

/**
 * This class provides static utility methods for working with member and class
 * modifiers (i.e., is a member declared {@code public} or {@code private}?).
 *
 * @since 1.4
 * @see java.lang.reflect.Modifier
 */
@API(status = MAINTAINED, since = "1.4")
public final class ModifierSupport {

	/**
	 * Returns {@code true} if the given class is declared {@code public}, {@code false} otherwise.
	 *
	 * @param clazz the class to check; never {@code null}
	 * @return true if the class is public, false otherwise
	 * @see java.lang.reflect.Modifier#isPublic(int)
	 */
	public static boolean isPublic(Class<?> clazz) {
		Preconditions.notNull(clazz, "Class must not be null");
		return ReflectionUtils.isPublic(clazz);
	}

	/**
	 * Returns {@code true} if the given member is declared {@code public}, {@code false} otherwise.
	 *
	 * @param member the member to check; never {@code null}
	 * @return true if the class is public, false otherwise
	 * @see java.lang.reflect.Modifier#isPublic(int)
	 */
	public static boolean isPublic(Member member) {
		Preconditions.notNull(member, "Member must not be null");
		return ReflectionUtils.isPublic(member);
	}

	/**
	 * Returns {@code true} if the given class is declared {@code private}, {@code false} otherwise.
	 *
	 * @param clazz the class to check; never {@code null}
	 * @return true if the class is private, false otherwise
	 * @see java.lang.reflect.Modifier#isPrivate(int)
	 */
	public static boolean isPrivate(Class<?> clazz) {
		Preconditions.notNull(clazz, "Class must not be null");
		return ReflectionUtils.isPrivate(clazz);
	}

	/**
	 * Returns {@code true} if the given member is declared {@code private}, {@code false} otherwise.
	 *
	 * @param member the member to check; never {@code null}
	 * @return true if the class is private, false otherwise
	 * @see java.lang.reflect.Modifier#isPrivate(int)
	 */
	public static boolean isPrivate(Member member) {
		Preconditions.notNull(member, "Member must not be null");
		return ReflectionUtils.isPrivate(member);
	}

	/**
	 * Returns {@code true} if the given member is not declared {@code private}, {@code false} otherwise.
	 *
	 * <p>In other words this method will return true for members declared public, protected or package private and
	 * false for members declared private.</p>
	 *
	 * @param member the member to check; never {@code null}
	 * @return true if the member is not private, false otherwise
	 * @see java.lang.reflect.Modifier#isPublic(int)
	 * @see java.lang.reflect.Modifier#isProtected(int)
	 * @see java.lang.reflect.Modifier#isPrivate(int)
	 */
	public static boolean isNotPrivate(Member member) {
		Preconditions.notNull(member, "Member must not be null");
		return ReflectionUtils.isNotPrivate(member);
	}

	/**
	 * Returns {@code true} if the given class is declared {@code abstract}, {@code false} otherwise.
	 *
	 * @param clazz the class to check; never {@code null}
	 * @return true if the class is abstract, false otherwise
	 * @see java.lang.reflect.Modifier#isAbstract(int)
	 */
	public static boolean isAbstract(Class<?> clazz) {
		Preconditions.notNull(clazz, "Class must not be null");
		return ReflectionUtils.isAbstract(clazz);
	}

	/**
	 * Returns {@code true} if the given member is declared {@code abstract}, {@code false} otherwise.
	 *
	 * @param member the class to check; never {@code null}
	 * @return true if the member is abstract, false otherwise
	 * @see java.lang.reflect.Modifier#isAbstract(int)
	 */
	public static boolean isAbstract(Member member) {
		Preconditions.notNull(member, "Member must not be null");
		return ReflectionUtils.isAbstract(member);
	}

	/**
	 * Returns {@code true} if the given class is declared {@code static}, {@code false} otherwise.
	 *
	 * @param clazz the class to check; never {@code null}
	 * @return true if the class is static, false otherwise
	 * @see java.lang.reflect.Modifier#isStatic(int)
	 */
	public static boolean isStatic(Class<?> clazz) {
		Preconditions.notNull(clazz, "Class must not be null");
		return ReflectionUtils.isStatic(clazz);
	}

	/**
	 * Returns {@code true} if the given member is declared {@code static}, {@code false} otherwise.
	 *
	 * @param member the member to check; never {@code null}
	 * @return true if the member is static, false otherwise
	 * @see java.lang.reflect.Modifier#isStatic(int)
	 */
	public static boolean isStatic(Member member) {
		Preconditions.notNull(member, "Member must not be null");
		return ReflectionUtils.isStatic(member);
	}

	/**
	 * Returns {@code true} if the given member is not declared {@code static}, {@code false} otherwise.
	 *
	 * @param member the member to check; never {@code null}
	 * @return true if the member is not static, false otherwise
	 * @see java.lang.reflect.Modifier#isStatic(int)
	 */
	public static boolean isNotStatic(Member member) {
		Preconditions.notNull(member, "Member must not be null");
		return ReflectionUtils.isNotStatic(member);
	}
}
