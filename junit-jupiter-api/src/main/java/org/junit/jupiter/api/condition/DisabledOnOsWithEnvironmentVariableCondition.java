package org.junit.jupiter.api.condition;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.platform.commons.util.Preconditions;

import java.util.Arrays;

import static java.lang.String.format;
import static org.junit.jupiter.api.extension.ConditionEvaluationResult.disabled;

/**
 * {@link ExecutionCondition} for {@link DisabledOnOsWithEnvironmentVariable @DisabledOnOSWithEnvironmentVariable}.
 *
 * @see DisabledOnOsWithEnvironmentVariable
 * @since 5.8
 */
public class DisabledOnOsWithEnvironmentVariableCondition
        extends AbstractRepeatableAnnotationCondition<DisabledOnOsWithEnvironmentVariable> {

    private static final ConditionEvaluationResult ENABLED = ConditionEvaluationResult.enabled(
            "No @DisabledOnOsWithEnvironmentVariable conditions resulting in 'disabled' execution encountered");

    DisabledOnOsWithEnvironmentVariableCondition() {
        super(DisabledOnOsWithEnvironmentVariable.class);
    }

    @Override
    protected ConditionEvaluationResult getNoDisabledConditionsEncounteredResult() {
        return ENABLED;
    }

    @Override
    protected ConditionEvaluationResult evaluate(final DisabledOnOsWithEnvironmentVariable annotation) {
        final OS[] operatingSystems = annotation.value();
        Preconditions.condition(operatingSystems.length > 0, "You must declare at least one OS in @DisabledOnOs");
        final boolean isDisabled = Arrays.stream(operatingSystems).anyMatch(OS::isCurrentOs);
        if (isDisabled) {
            final String name = annotation.named();
            final String regex = annotation.matches();
            Preconditions.notBlank(name, () -> "The 'named' attribute must not be blank in " + annotation);
            Preconditions.notBlank(regex, () -> "The 'matches' attribute must not be blank in " + annotation);

            String actual = getEnvironmentVariable(name);
            if (actual == null) {
                return ENABLED;
            }

            final boolean matches = actual.matches(regex);
            if (matches) {
                return disabled(
                        format(
                                "Disabled on operating system: [%s] with Environment variable [%s] with " +
                                        "value [%s] matches regular expression [%s]",
                                System.getProperty("os.name"), name, actual, regex
                        ), annotation.disabledReason());
            }
        }
        return ENABLED;
    }

    /**
     * Get the value of the named environment variable.
     *
     * <p>The default implementation simply delegates to
     * {@link System#getenv(String)}. Can be overridden in a subclass for
     * testing purposes.
     */
    protected String getEnvironmentVariable(String name) {
        return System.getenv(name);
    }
}
