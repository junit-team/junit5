import org.gradle.api.Task
import org.gradle.internal.os.OperatingSystem

fun Task.trackOperationSystemAsInput() =
    inputs.property("os", OperatingSystem.current().familyName)
