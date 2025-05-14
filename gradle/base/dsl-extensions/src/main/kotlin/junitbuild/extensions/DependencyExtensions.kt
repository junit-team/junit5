package junitbuild.extensions

import org.gradle.api.provider.Provider
import org.gradle.plugin.use.PluginDependency

// see https://docs.gradle.org/current/userguide/plugins.html#sec:plugin_markers
val Provider<PluginDependency>.markerCoordinates: Provider<String>
    get() = map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}" }
