package org.junit.jupiter.api.extension;
import org.apiguardian.api.API;
import org.junit.jupiter.api.AutoCloseUtils;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
/**
 * The {@code AutoCloseExtension} class is a JUnit 5 extension that automatically closes resources used in tests.
 *
 * <p>
 * This extension implements the {@link org.junit.jupiter.api.extension.AfterEachCallback} interface,
 * allowing it to perform resource cleanup after each test execution. It invokes the
 * {@link AutoCloseUtils#closeResources(Object)} method to close the resources annotated with
 * {@link org.junit.jupiter.api.AutoClose}.
 * </p>
 *
 * <p>
 * To use this extension, annotate your test class or test method with {@link org.junit.jupiter.api.extension.ExtendWith}
 * and provide an instance of {@code AutoCloseExtension}.
 * </p>
 *
 * @see org.junit.jupiter.api.extension.AfterEachCallback
 * @see org.junit.jupiter.api.extension.Extension
 * @see org.junit.jupiter.api.extension.ExtensionContext
 * @see org.junit.jupiter.api.AutoClose
 * @see AutoCloseUtils#closeResources(Object)
 */
@API(status = API.Status.EXPERIMENTAL,since = "5.9")

public class AutoCloseExtension implements AfterEachCallback {
    /**
     * Invoked after each test execution to close the annotated resources within the test instance.
     *
     * @param context the extension context for the current test execution
     * @throws Exception if an exception occurs during resource cleanup
     */
    AutoCloseExtension(){

    }
    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        Object testInstance = context.getRequiredTestInstance();
        AutoCloseUtils.closeResources(testInstance);
    }
}
