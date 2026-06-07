package com.aicontext.plugin.services

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

@Service
@State(name = "CopyProjectContextSettings", storages = [Storage("copyProjectContext.xml")])
class SettingsService : PersistentStateComponent<SettingsService.State> {

    data class State(
        var maxFiles: Int = DEFAULT_MAX_FILES,
        var maxCharacters: Int = DEFAULT_MAX_CHARACTERS,
        var maxSingleFileSize: Int = DEFAULT_MAX_SINGLE_FILE_SIZE,
        var excludedDirectories: MutableList<String> = defaultExcludedDirectories(),
        var excludedExtensions: MutableList<String> = defaultExcludedExtensions(),
        var excludedFilePatterns: MutableList<String> = defaultExcludedFilePatterns(),
        var includeProjectTree: Boolean = true,
        var includeFileContents: Boolean = true,
    )

    private var state = State()

    override fun getState(): State = state

    override fun loadState(state: State) {
        XmlSerializerUtil.copyBean(state, this.state)
        ensureDefaultsPresent()
    }

    fun getMaxFiles(): Int = state.maxFiles

    fun getMaxCharacters(): Int = state.maxCharacters

    fun getMaxSingleFileSize(): Int = state.maxSingleFileSize

    fun getExcludedDirectories(): Set<String> = state.excludedDirectories.map { it.trim() }.filter { it.isNotEmpty() }.toSet()

    fun getExcludedExtensions(): Set<String> = state.excludedExtensions.map { normalizeExtension(it) }.filter { it.isNotEmpty() }.toSet()

    fun getExcludedFilePatterns(): Set<String> =
        state.excludedFilePatterns.map { it.trim() }.filter { it.isNotEmpty() }.toSet()

    fun getExcludedDirectoriesList(): List<String> = state.excludedDirectories.toList()

    fun getExcludedExtensionsList(): List<String> = state.excludedExtensions.toList()

    fun isIncludeProjectTree(): Boolean = state.includeProjectTree

    fun isIncludeFileContents(): Boolean = state.includeFileContents

    fun updateSettings(
        maxFiles: Int,
        maxCharacters: Int,
        maxSingleFileSize: Int,
        excludedDirectories: List<String>,
        excludedExtensions: List<String>,
        excludedFilePatterns: List<String>,
        includeProjectTree: Boolean,
        includeFileContents: Boolean,
    ) {
        state.maxFiles = maxFiles.coerceAtLeast(1)
        state.maxCharacters = maxCharacters.coerceAtLeast(1)
        state.maxSingleFileSize = maxSingleFileSize.coerceAtLeast(1)
        state.excludedDirectories = excludedDirectories.map { it.trim() }.filter { it.isNotEmpty() }.toMutableList()
        state.excludedExtensions = excludedExtensions.map { normalizeExtension(it) }.filter { it.isNotEmpty() }.toMutableList()
        state.excludedFilePatterns = excludedFilePatterns.map { it.trim() }.filter { it.isNotEmpty() }.toMutableList()
        state.includeProjectTree = includeProjectTree
        state.includeFileContents = includeFileContents
        ensureDefaultsPresent()
    }

    fun resetToDefaults() {
        state = State()
    }

    private fun ensureDefaultsPresent() {
        if (state.excludedDirectories.isEmpty()) {
            state.excludedDirectories = defaultExcludedDirectories()
        }
        if (state.excludedExtensions.isEmpty()) {
            state.excludedExtensions = defaultExcludedExtensions()
        }
        if (state.excludedFilePatterns.isEmpty()) {
            state.excludedFilePatterns = defaultExcludedFilePatterns()
        }
    }

    companion object {
        const val DEFAULT_MAX_FILES = 1000
        const val DEFAULT_MAX_CHARACTERS = 5_000_000
        const val DEFAULT_MAX_SINGLE_FILE_SIZE = 500_000

        fun getInstance(): SettingsService = ApplicationManager.getApplication().getService(SettingsService::class.java)

        fun defaultExcludedDirectories(): MutableList<String> = mutableListOf(
            ".idea",
            ".git",
            ".gradle",
            "build",
            "out",
            "target",
            "dist",
            "coverage",
            "node_modules",
            "vendor",
        )

        fun defaultExcludedExtensions(): MutableList<String> = mutableListOf(
            "class",
            "jar",
            "war",
            "ear",
            "log",
            "tmp",
            "lock",
            "png",
            "jpg",
            "jpeg",
            "gif",
            "bmp",
            "ico",
            "webp",
            "pdf",
            "zip",
            "rar",
            "7z",
            "tar",
            "gz",
            "exe",
            "dll",
            "so",
            "dylib",
        )

        fun defaultExcludedFilePatterns(): MutableList<String> = mutableListOf(
            ".DS_Store",
        )

        private fun normalizeExtension(extension: String): String {
            val trimmed = extension.trim().lowercase()
            return trimmed.removePrefix("*.").removePrefix(".")
        }
    }
}
