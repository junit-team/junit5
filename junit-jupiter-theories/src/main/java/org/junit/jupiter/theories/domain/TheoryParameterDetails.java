package org.junit.jupiter.theories.domain;

import org.junit.platform.commons.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.util.*;

/**
 * Domain object that contains all of the information about a theory parameter.
 */
public class TheoryParameterDetails {
    private final int index;
    private final Class<?> type;
    private final Class<?> nonPrimitiveType;
    private final String name;
    private final List<String> qualifiers;
    private final Optional<? extends Annotation> parameterSupplierAnnotation;


    /**
     * Constructor.
     *
     * @param index the index of this parameter in the theory's arguments
     * @param type the type for this parameter
     * @param name the name of this parameter
     * @param qualifiers the qualifiers (if any) for this parameter
     * @param argumentSupplierAnnotation the parameter argument supplier annotation (if any) for this parameter
     */
    public TheoryParameterDetails(int index, Class<?> type, String name, List<String> qualifiers, Optional<? extends Annotation> argumentSupplierAnnotation) {
        this.index = index;
        this.type = type;
        this.nonPrimitiveType = ReflectionUtils.getBoxedClass(type);
        this.name = name;
        this.qualifiers = Collections.unmodifiableList(new ArrayList<>(qualifiers));
        this.parameterSupplierAnnotation = argumentSupplierAnnotation;
    }

    /**
     * @return the index of this parameter in the theory's arguments
     */
    public int getIndex() {
        return index;
    }

    /**
     * @return the type for this parameter
     */
    public Class<?> getType() {
        return type;
    }

    /**
     * @return the non-primitive type for this parameter (this will either be the original type, or its boxed equivalent if the original type was a primitive)
     */
    public Class<?> getNonPrimitiveType() {
        return nonPrimitiveType;
    }

    /**
     * @return the name of this parameter
     */
    public String getName() {
        return name;
    }

    /**
     * @return the qualifiers (if any) for this parameter
     */
    public List<String> getQualifiers() {
        return qualifiers;
    }

    /**
     * @return the parameter argument supplier annotation (if any) for this parameter
     */
    public Optional<? extends Annotation> getArgumentSupplierAnnotation() {
        return parameterSupplierAnnotation;
    }

    @Override
    public String toString() {
        return "TheoryParameterDetails{" +
                "index=" + index +
                ", type=" + type +
                ", nonPrimitiveType=" + nonPrimitiveType +
                ", name='" + name + '\'' +
                ", qualifiers=" + qualifiers +
                ", parameterSupplierAnnotation=" + parameterSupplierAnnotation +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof TheoryParameterDetails)) {
            return false;
        }
        TheoryParameterDetails other = (TheoryParameterDetails) o;
        return index == other.index
                && Objects.equals(type, other.type)
                && Objects.equals(nonPrimitiveType, other.nonPrimitiveType)
                && Objects.equals(name, other.name)
                && Objects.equals(qualifiers, other.qualifiers)
                && Objects.equals(parameterSupplierAnnotation, other.parameterSupplierAnnotation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, type, nonPrimitiveType, name, qualifiers, parameterSupplierAnnotation);
    }
}
