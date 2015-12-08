
package org.junit.gen5.engine;

import java.util.HashMap;
import java.util.Map;

public class Context {

	private final Map<String, Object> map;

	public Context() {
		this(new HashMap<>());
	}

	private Context(Map<String, Object> map) {
		this.map = map;
	}

	public Context with(String key, Object value) {
		Map<String, Object> newMap = new HashMap<>(map);
		newMap.put(key, value);
		return new Context(newMap);
	}

	public <T> T get(String key, Class<T> clazz) {
		return clazz.cast(map.get(key));
	}

}
