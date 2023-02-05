package junitbuild.javadoc

import org.gradle.external.javadoc.JavadocOptionFileOption
import org.gradle.external.javadoc.internal.JavadocOptionFileWriterContext

class ModuleSpecificJavadocFileOption(private val option: String, private var valuePerModule: Map<String, String>) : JavadocOptionFileOption<Map<String, String>> {

    override fun getOption() = option

    override fun getValue() = valuePerModule

    override fun setValue(value: Map<String, String>) {
        this.valuePerModule = value
    }

    override fun write(writerContext: JavadocOptionFileWriterContext) {
        valuePerModule.forEach { (moduleName, value) ->
            writerContext
                    .writeOptionHeader(option)
                    .write(moduleName)
                    .write("=")
                    .write(value)
                    .newLine()
        }
    }
}
