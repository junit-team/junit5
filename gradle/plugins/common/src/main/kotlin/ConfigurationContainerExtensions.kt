
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.kotlin.dsl.NamedDomainObjectContainerCreatingDelegateProvider

val ConfigurationContainer.creatingResolvable
    get() = creatingResolvable {}

fun ConfigurationContainer.creatingResolvable(configuration: Configuration.() -> Unit) =
    NamedDomainObjectContainerCreatingDelegateProvider.of(this) {
        isCanBeResolved = true
        isCanBeConsumed = false
        configuration()
    }
