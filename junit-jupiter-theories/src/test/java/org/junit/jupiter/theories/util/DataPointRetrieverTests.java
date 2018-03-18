package org.junit.jupiter.theories.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.theories.annotations.DataPoint;
import org.junit.jupiter.theories.annotations.DataPoints;
import org.junit.jupiter.theories.domain.DataPointDetails;
import org.junit.jupiter.theories.exceptions.DataPointRetrievalException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DataPointRetrieverTests {

    private DataPointRetriever retrieverUnderTest;

    @BeforeEach
    public void setUp() {
        retrieverUnderTest = new DataPointRetriever();
    }

    @Test
    public void testGetAllDataPoints_InstanceDataPoints() {
        //Setup
        ClassWithValidInstanceDataPoints dataPointsSourceObject = new ClassWithValidInstanceDataPoints();
        List<String> expectedResults = dataPointsSourceObject.getExpectedValues();

        //Test
        List<DataPointDetails> actualResults = retrieverUnderTest.getAllDataPoints(ClassWithValidInstanceDataPoints.class, Optional.of(dataPointsSourceObject));

        //Verify
        List<String> actualValues = actualResults.stream()
                .map(DataPointDetails::getValue)
                .peek(v -> assertThat(v).isInstanceOf(String.class))
                .map(v -> (String) v)
                .collect(toList());

        assertThat(actualValues)
                .containsExactlyInAnyOrderElementsOf(expectedResults);
    }

    @Test
    public void testGetAllDataPoints_StaticDataPoints_WithInstance() {
        //Setup
        ClassWithValidStaticDataPoints dataPointsSourceObject = new ClassWithValidStaticDataPoints();
        List<String> expectedResults = dataPointsSourceObject.getExpectedValues();

        //Test
        List<DataPointDetails> actualResults = retrieverUnderTest.getAllDataPoints(ClassWithValidStaticDataPoints.class, Optional.of(dataPointsSourceObject));

        //Verify
        List<String> actualValues = actualResults.stream()
                .map(DataPointDetails::getValue)
                .peek(v -> assertThat(v).isInstanceOf(String.class))
                .map(v -> (String) v)
                .collect(toList());

        assertThat(actualValues)
                .containsExactlyInAnyOrderElementsOf(expectedResults);
    }

    @Test
    public void testGetAllDataPoints_StaticDataPoints_WithoutInstance() {
        //Setup
        ClassWithValidStaticDataPoints dataPointsSourceObject = new ClassWithValidStaticDataPoints();
        List<String> expectedResults = dataPointsSourceObject.getExpectedValues();

        //Test
        List<DataPointDetails> actualResults = retrieverUnderTest.getAllDataPoints(ClassWithValidStaticDataPoints.class, Optional.empty());

        //Verify
        List<String> actualValues = actualResults.stream()
                .map(DataPointDetails::getValue)
                .peek(v -> assertThat(v).isInstanceOf(String.class))
                .map(v -> (String) v)
                .collect(toList());

        assertThat(actualValues)
                .containsExactlyInAnyOrderElementsOf(expectedResults);
    }

    @Test
    public void testGetAllDataPoints_ThrowsExceptionOnMissingInstanceForInstanceDataPoint() {
        //Test/Verify
        assertThatThrownBy(() -> retrieverUnderTest.getAllDataPoints(ClassWithValidInstanceDataPoints.class, Optional.empty()))
                .isInstanceOf(DataPointRetrievalException.class)
                .hasMessageContaining("instance was not available");
    }

    @Test
    public void testGetAllDataPoints_DataPointMethodThrowsException() {
        //Test/Verify
        assertThatThrownBy(() -> retrieverUnderTest.getAllDataPoints(ClassWithDataPointMethodThatThrowsException.class, Optional.empty()))
                .isInstanceOf(DataPointRetrievalException.class)
                .hasMessageContaining(ClassWithDataPointMethodThatThrowsException.EXCEPTION_MESSAGE);
    }

    @Test
    public void testGetAllDataPoints_InvalidDataPointsGroupType() {
        //Test/Verify
        assertThatThrownBy(() -> retrieverUnderTest.getAllDataPoints(ClassWithDataPointsOfInvalidGroupType.class, Optional.empty()))
                .isInstanceOf(DataPointRetrievalException.class)
                .hasMessageContaining("not a recognized group of datapoints");
    }

    //-------------------------------------------------------------------------
    // Test helper methods/classes
    //-------------------------------------------------------------------------
    private static class ClassWithValidInstanceDataPoints {
        public List<String> getExpectedValues() {
            List<String> expectedValues = new ArrayList<>();

            expectedValues.add(publicInstanceStringField);
            expectedValues.add(publicInstanceStringMethod());

            expectedValues.add(privateInstanceStringField);
            expectedValues.add(privateInstanceStringMethod());

            expectedValues.addAll(publicInstanceListField);
            expectedValues.addAll(publicInstanceListMethod());

            expectedValues.addAll(privateInstanceListField);
            expectedValues.addAll(privateInstanceListMethod());

            expectedValues.addAll(Arrays.asList(publicInstanceArrayField));
            expectedValues.addAll(Arrays.asList(publicInstanceArrayMethod()));

            expectedValues.addAll(Arrays.asList(privateInstanceArrayField));
            expectedValues.addAll(Arrays.asList(privateInstanceArrayMethod()));

            return expectedValues;
        }


        //Public data point
        @DataPoint
        public final String publicInstanceStringField = "public instance string field";

        @DataPoint
        public String publicInstanceStringMethod() {
            return "public instance string method";
        }


        //Private data point
        @DataPoint
        private final String privateInstanceStringField = "private instance string field";

        @DataPoint
        private String privateInstanceStringMethod() {
            return "private instance string method";
        }

        //Public list data points
        @DataPoints
        private final List<String> publicInstanceListField = Arrays.asList("public instance list field (1/2)", "public instance list field (1/2)");

        @DataPoints
        public List<String> publicInstanceListMethod() {
            return Arrays.asList("public instance list method (1/2)", "public instance list method (2/2)");
        }

        //Private list data points
        @DataPoints
        private final List<String> privateInstanceListField = Arrays.asList("private instance list field (1/2)", "private instance list field (1/2)");

        @DataPoints
        private List<String> privateInstanceListMethod() {
            return Arrays.asList("private instance list method (1/2)", "private instance list method (2/2)");
        }

        //Public array data points
        @DataPoints
        public final String[] publicInstanceArrayField = {"public instance array field (1/2)", "public instance array field (1/2)"};

        @DataPoints
        public String[] publicInstanceArrayMethod() {
            String[] result = {"public instance array method (1/2)", "public instance array method (2/2)"};
            return result;
        }

        //Private array data points
        @DataPoints
        private final String[] privateInstanceArrayField = {"private instance array field (1/2)", "private instance array field (1/2)"};

        @DataPoints
        private String[] privateInstanceArrayMethod() {
            String[] result = {"private instance array method (1/2)", "private instance array method (2/2)"};
            return result;
        }
    }

    private static class ClassWithValidStaticDataPoints {
        public List<String> getExpectedValues() {
            List<String> expectedValues = new ArrayList<>();

            expectedValues.add(PUBLIC_STATIC_STRING_FIELD);
            expectedValues.add(publicStaticStringMethod());

            expectedValues.add(PRIVATE_STATIC_STRING_FIELD);
            expectedValues.add(privateStaticStringMethod());

            expectedValues.addAll(PUBLIC_STATIC_LIST_FIELD);
            expectedValues.addAll(publicStaticListMethod());

            expectedValues.addAll(PRIVATE_STATIC_LIST_FIELD);
            expectedValues.addAll(privateStaticListMethod());

            expectedValues.addAll(Arrays.asList(PUBLIC_STATIC_ARRAY_FIELD));
            expectedValues.addAll(Arrays.asList(publicStaticArrayMethod()));

            expectedValues.addAll(Arrays.asList(PRIVATE_STATIC_ARRAY_FIELD));
            expectedValues.addAll(Arrays.asList(privateStaticArrayMethod()));

            return expectedValues;
        }


        //Public data point
        @DataPoint
        public static final String PUBLIC_STATIC_STRING_FIELD = "public static string field";

        @DataPoint
        public static String publicStaticStringMethod() {
            return "public static string method";
        }


        //Private data point
        @DataPoint
        private static final String PRIVATE_STATIC_STRING_FIELD = "private static string field";

        @DataPoint
        private static String privateStaticStringMethod() {
            return "private static string method";
        }


        //Public list data points
        @DataPoints
        public static final List<String> PUBLIC_STATIC_LIST_FIELD = Arrays.asList("public static list field (1/2)", "public static list field (2/2)");

        @DataPoints
        public static List<String> publicStaticListMethod() {
            return Arrays.asList("public static list method (1/2)", "public static list method (2/2)");
        }


        //Private list data points
        @DataPoints
        private static final List<String> PRIVATE_STATIC_LIST_FIELD = Arrays.asList("private static list field (1/2)", "private static list field (2/2)");

        @DataPoints
        private static List<String> privateStaticListMethod() {
            return Arrays.asList("private static list method (1/2)", "private static list method (2/2)");
        }


        //Public array data points
        @DataPoints
        public static final String[] PUBLIC_STATIC_ARRAY_FIELD = {"public static array field (1/2)", "public static array field (2/2)"};

        @DataPoints
        public static String[] publicStaticArrayMethod() {
            String[] result = {"public static array method (1/2)", "public static array method (2/2)"};
            return result;
        }


        //Private array data points
        @DataPoints
        private static final String[] PRIVATE_STATIC_ARRAY_FIELD = {"private static array field (1/2)", "private static array field (2/2)"};

        @DataPoints
        private static String[] privateStaticArrayMethod() {
            String[] result = {"private static array method (1/2)", "private static array method (2/2)"};
            return result;
        }
    }

    private static class ClassWithDataPointMethodThatThrowsException {
        public static final String EXCEPTION_MESSAGE = "Test exception";

        @DataPoint
        public static String throwException() {
            throw new RuntimeException(EXCEPTION_MESSAGE);
        }
    }

    private static class ClassWithDataPointsOfInvalidGroupType {
        @DataPoints
        public static String THIS_IS_NOT_VALID = "Because string is not a valid data points group type";
    }
}