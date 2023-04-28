import org.gradle.api.file.RegularFile
import java.net.URI

data class License(val name: String, val url: URI, val headerFile: RegularFile)
