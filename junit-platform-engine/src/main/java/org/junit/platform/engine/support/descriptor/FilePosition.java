/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.support.descriptor;

import java.util.Optional;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.util.StringUtils;

/**
 * Position inside a file represented by {@linkplain #getLine line} and
 * {@linkplain #getColumn column} numbers.
 *
 * @since 1.0
 * @deprecated use {@link org.junit.platform.engine.discovery.FilePosition}
 */
@API(status = Status.DEPRECATED, since = "1.7")
@Deprecated
public class FilePosition extends org.junit.platform.engine.discovery.FilePosition {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(FilePosition.class);

	private FilePosition(int line) {
		super(line);
	}

	private FilePosition(int line, int column) {
		super(line, column);
	}

	/**
	 * Create a new {@code FilePosition} using the supplied {@code line} number
	 * and an undefined column number.
	 *
	 * @param line the line number; must be greater than zero
	 * @return a {@link FilePosition} with the given line number
	 */
	public static FilePosition from(int line) {
		return new FilePosition(line);
	}

	/**
	 * Create a new {@code FilePosition} using the supplied {@code line} and
	 * {@code column} numbers.
	 *
	 * @param line the line number; must be greater than zero
	 * @param column the column number; must be greater than zero
	 * @return a {@link FilePosition} with the given line and column numbers
	 */
	public static FilePosition from(int line, int column) {
		return new FilePosition(line, column);
	}

	/**
	 * Create an optional {@code FilePosition} by parsing the supplied
	 * {@code query} string.
	 *
	 * <p>Examples of valid {@code query} strings:
	 * <ul>
	 *     <li>{@code "line=23"}</li>
	 *     <li>{@code "line=23&column=42"}</li>
	 * </ul>
	 *
	 * @param query the query string; may be {@code null}
	 * @return an {@link Optional} containing a {@link FilePosition} with
	 * the parsed line and column numbers; never {@code null} but potentially
	 * empty
	 * @since 1.3
	 * @see #from(int)
	 * @see #from(int, int)
	 */
	public static Optional<org.junit.platform.engine.discovery.FilePosition> fromQuery(String query) {
		FilePosition result = null;
		Integer line = null;
		Integer column = null;
		if (StringUtils.isNotBlank(query)) {
			try {
				for (String pair : query.split("&")) {
					String[] data = pair.split("=");
					if (data.length == 2) {
						String key = data[0];
						if (line == null && "line".equals(key)) {
							line = Integer.valueOf(data[1]);
						}
						else if (column == null && "column".equals(key)) {
							column = Integer.valueOf(data[1]);
						}
					}

					// Already found what we're looking for?
					if (line != null && column != null) {
						break;
					}
				}
			}
			catch (IllegalArgumentException ex) {
				logger.debug(ex, () -> "Failed to parse 'line' and/or 'column' from query string: " + query);
				// fall-through and continue
			}

			if (line != null) {
				result = column == null ? new FilePosition(line) : new FilePosition(line, column);
			}
		}
		return Optional.ofNullable(result);
	}

}
