import org.gradle.api.DomainObjectSet
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.domainObjectSet
import javax.inject.Inject

open class MultiReleaseSourcesExtension @Inject constructor(objects: ObjectFactory) {
	val releases: DomainObjectSet<Int> = objects.domainObjectSet(Int::class)
}
