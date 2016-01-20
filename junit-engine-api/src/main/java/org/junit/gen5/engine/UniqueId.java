package org.junit.gen5.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.junit.gen5.commons.util.StringUtils;

public class UniqueId {

	public static final String TYPE_ENGINE = "engine";

	private static final String SEGMENT_DELIMITER = "/";

	private final List<Segment> segments = new ArrayList<>();

	public UniqueId(String engineId) {
		segments.add(new Segment(TYPE_ENGINE, engineId));
	}

	public String getUniqueString() {
		Stream<String> segmentStream = segments.stream().map(this::describe);
		return StringUtils.join(segmentStream, SEGMENT_DELIMITER);
	}

	private String describe(Segment segment) {
		return String.format("[%s:%s]", segment.getType(), segment.getValue());
	}

	public List<Segment> getSegments() {
		return Collections.unmodifiableList(segments);
	}

	public static class Segment {

		private final String type;
		private final String value;

		private Segment(String type, String value) {
			this.type = type;
			this.value = value;
		}

		public String getType() {
			return type;
		}

		public String getValue() {
			return value;
		}
	}
}
