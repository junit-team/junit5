/*
 * Copyright 2015-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package de.sormuras.bartholdy;

import java.io.File;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Reflector {

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface Option {
		String value();
	}

	public static List<String> reflect(Object options) {
		var arguments = new ArrayList<String>();
		new Reflector(options, arguments::add).reflect();
		return List.copyOf(arguments);
	}

	private final Object options;
	private final UnaryOperator<Stream<Field>> operator;
	private final Consumer<String> consumer;

	public Reflector(Object options, Consumer<String> consumer) {
		this(options, UnaryOperator.identity(), consumer);
	}

	public Reflector(Object options, UnaryOperator<Stream<Field>> operator, Consumer<String> consumer) {
		this.options = options;
		this.operator = operator;
		this.consumer = consumer;
	}

	public Reflector add(Object argument) {
		consumer.accept(argument.toString());
		return this;
	}

	/** Add single argument composed of joined path names using {@link File#pathSeparator}. */
	Reflector add(Collection<Path> paths) {
		return add(paths.stream(), File.pathSeparator);
	}

	/** Add single argument composed of all stream elements joined by specified separator. */
	Reflector add(Stream<?> stream, String separator) {
		return add(stream.map(Object::toString).collect(Collectors.joining(separator)));
	}

	/** Visit reflected options after a custom stream operator did its work. */
	public void reflect() {
		var stream = Arrays.stream(options.getClass().getDeclaredFields()).filter(field -> !field.isSynthetic()).filter(
			field -> !Modifier.isStatic(field.getModifiers())).filter(
				field -> !Modifier.isPrivate(field.getModifiers())).filter(
					field -> !Modifier.isTransient(field.getModifiers()));
		stream = operator.apply(stream);
		stream.forEach(this::reflectField);
	}

	private void reflectField(Field field) {
		try {
			reflectFieldThrowing(field);
		}
		catch (ReflectiveOperationException e) {
			throw new Error("reflecting field '" + field + "' failed for " + options, e);
		}
	}

	private void reflectFieldThrowing(Field field) throws ReflectiveOperationException {

		// custom option reflector method declared?
		try {
			options.getClass().getDeclaredMethod(field.getName(), Reflector.class).invoke(options, this);
			return;
		}
		catch (NoSuchMethodException e) {
			// fall-through
		}

		// get the field's value
		var value = field.get(options);
		// skip null field value
		if (value == null) {
			return;
		}
		// skip empty collections
		if (value instanceof Collection && ((Collection) value).isEmpty()) {
			return;
		}

		//    // common add helper available?
		//    try {
		//      Helper.class.getDeclaredMethod(field.getName(), field.getType()).invoke(helper, value);
		//      return;
		//    } catch (NoSuchMethodException e) {
		//      // fall-through
		//    }

		// get or generate option name
		var optional = Optional.ofNullable(field.getAnnotation(Option.class));
		var optionName = optional.map(Option::value).orElse(getOptionName(field.getName()));

		// is it an omissible boolean flag?
		if (field.getType() == boolean.class) {
			if (field.getBoolean(options)) {
				add(optionName);
			}
			return;
		}
		// add option name only if it is not empty
		if (!optionName.isEmpty()) {
			add(optionName);
		}
		// is value a collection?
		if (value instanceof Collection) {
			var iterator = ((Collection) value).iterator();
			var head = iterator.next();

			//      if (field.isAnnotationPresent(Repeatable.class)) {
			//        add(head);
			//        while (iterator.hasNext()) {
			//          add(optionName);
			//          add(iterator.next());
			//        }
			//        return;
			//      }

			if (head instanceof Path) {
				@SuppressWarnings("unchecked")
				var paths = (Collection<Path>) value;
				add(paths);
				return;
			}
		}
		// finally, add string representation of the value
		add(value.toString());
	}

	private String getOptionName(String fieldName) {
		var hasUppercase = !fieldName.equals(fieldName.toLowerCase());
		var defaultName = new StringBuilder();
		if (hasUppercase) {
			defaultName.append("--");
			fieldName.chars().forEach(i -> {
				if (Character.isUpperCase(i)) {
					defaultName.append('-');
					defaultName.append((char) Character.toLowerCase(i));
				}
				else {
					defaultName.append((char) i);
				}
			});
		}
		else {
			defaultName.append('-');
			defaultName.append(fieldName.replace('_', '-'));
		}
		return defaultName.toString();
	}
}
