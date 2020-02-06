package org.junit.gradle.javadoc

import org.gradle.api.file.FileCollection
import org.gradle.external.javadoc.JavadocOptionFileOption
import org.gradle.external.javadoc.internal.JavadocOptionFileWriterContext

class FileCollectionAsPathJavadocFileOption(private val option: String, private var value: FileCollection?) : JavadocOptionFileOption<FileCollection> {

    override fun getOption() = option

    override fun getValue() = value

    override fun setValue(value: FileCollection?) {
        this.value = value
    }

    override fun write(writerContext: JavadocOptionFileWriterContext) {
        writerContext.writeValueOption(option, value!!.asPath)
    }

}
