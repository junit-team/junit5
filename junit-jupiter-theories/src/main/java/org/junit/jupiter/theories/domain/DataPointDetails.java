package org.junit.jupiter.theories.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Domain object that contains all of the information about a data point.
 */
public class DataPointDetails {
    private final Object value;
    private final List<String> qualifiers;
    private final String sourceName;


    /**
     * Constructor.
     *
     * @param value this data point's value
     * @param qualifiers qualifiers (if any) for this data point
     * @param sourceName the name of the source that produced this data point
     */
    public DataPointDetails(Object value, List<String> qualifiers, String sourceName) {
        this.value = value;
        this.qualifiers = qualifiers;
        this.sourceName = sourceName;
    }


    /**
     * @return this data point's value
     */
    public Object getValue() {
        return value;
    }


    /**
     * @return qualifiers (if any) for this data point
     */
    public List<String> getQualifiers() {
        return new ArrayList<>(qualifiers);
    }


    /**
     * @return the name of the source that produced this data point
     */
    public String getSourceName() {
        return sourceName;
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append(value)
                .append(" (Source: ")
                .append(sourceName)
                .append(")")
                .toString();
    }
}
