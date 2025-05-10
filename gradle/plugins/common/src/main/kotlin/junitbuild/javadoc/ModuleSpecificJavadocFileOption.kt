package junitbuild.javadoc

import org.gradle.api.provider.Provider
import org.gradle.external.javadoc.JavadocOptionFileOption
import org.gradle.external.javadoc.internal.JavadocOptionFileWriterContext

class ModuleSpecificJavadocFileOption(private val option: String, private var valuePerModule: Map<String, Provider<String>>) : JavadocOptionFileOption<Map<String, Provider<String>>> {

    override fun getOption() = option

    override fun getValue() = valuePerModule

    override fun setValue(value: Map<String, Provider<String>>) {
        this.valuePerModule = value
    }

    override fun write(writerContext: JavadocOptionFileWriterContext) {
        valuePerModule.forEach { (moduleName, value) ->
            writerContext
                    .writeOptionHeader(option)
                    .write(moduleName)
                    .write("=")
                    .write(value.get())
                    .newLine()
        }
    }
}
