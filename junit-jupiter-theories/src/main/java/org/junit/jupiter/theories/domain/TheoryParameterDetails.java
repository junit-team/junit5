package org.junit.jupiter.theories.domain;

import org.junit.platform.commons.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;

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
     * @param parameterSupplierAnnotation the data point parameter supplier annotation (if any) for this parameter
     */
    public TheoryParameterDetails(int index, Class<?> type, String name, List<String> qualifiers, Optional<? extends Annotation> parameterSupplierAnnotation) {
        this.index = index;
        this.type = type;
        this.nonPrimitiveType = ReflectionUtils.getBoxedClass(type);
        this.name = name;
        this.qualifiers = qualifiers;
        this.parameterSupplierAnnotation = parameterSupplierAnnotation;
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
     * @return the data point parameter supplier annotation (if any) for this parameter
     */
    public Optional<? extends Annotation> getParameterSupplierAnnotation() {
        return parameterSupplierAnnotation;
    }
}
