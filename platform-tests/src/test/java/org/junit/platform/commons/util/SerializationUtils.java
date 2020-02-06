/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.commons.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class SerializationUtils {

	@SuppressWarnings("unchecked")
	public static <T extends Serializable> T serializeAndDeserialize(T object) throws Exception {
		byte[] bytes = serialize(object);
		return (T) deserialize(bytes);
	}

	private static Object deserialize(byte[] bytes) throws Exception {
		try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
			return in.readObject();
		}
	}

	private static byte[] serialize(Object object) throws Exception {
		try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
			objectOutputStream.writeObject(object);
			objectOutputStream.flush();
			return byteArrayOutputStream.toByteArray();
		}
	}
}
