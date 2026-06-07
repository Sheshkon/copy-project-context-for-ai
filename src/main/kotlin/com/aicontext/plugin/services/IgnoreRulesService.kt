package com.aicontext.plugin.services

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileVisitor
import java.nio.charset.StandardCharsets
import java.nio.file.FileSystems
import java.nio.file.PathMatcher
import java.util.Locale

class IgnoreRulesService(
    private val project: Project,
    private val settingsService: SettingsService,
) {
    private val logger = Logger.getInstance(IgnoreRulesService::class.java)
    private val ignoreRuleSets: List<IgnoreRuleSet> = loadIgnoreRuleSets()
    private val globMatchers: List<GlobMatcher> = buildGlobMatchers()

    fun shouldSkipDirectory(directoryName: String): Boolean {
        return settingsService.getExcludedDirectories().contains(directoryName)
    }

    fun isIgnored(relativePath: String, isDirectory: Boolean): Boolean {
        val normalizedPath = normalizePath(relativePath)
        if (normalizedPath.isEmpty()) {
            return false
        }

        val firstSegment = normalizedPath.substringBefore('/')
        if (isDirectory && shouldSkipDirectory(firstSegment)) {
            return true
        }

        if (!isDirectory) {
            val fileName = normalizedPath.substringAfterLast('/')
            if (settingsService.getExcludedFilePatterns().contains(fileName)) {
                return true
            }

            val extension = fileName.substringAfterLast('.', "")
            if (extension.isNotEmpty() && settingsService.getExcludedExtensions().contains(extension.lowercase(Locale.getDefault()))) {
                return true
            }

            for (matcher in globMatchers) {
                if (matcher.matches(normalizedPath, isDirectory = false)) {
                    return true
                }
            }
        }

        for (ruleSet in ignoreRuleSets) {
            if (ruleSet.isIgnored(normalizedPath, isDirectory)) {
                return true
            }
        }

        return false
    }

    private fun loadIgnoreRuleSets(): List<IgnoreRuleSet> {
        val projectRoot = getProjectRoot() ?: return emptyList()
        val ruleSets = mutableListOf<IgnoreRuleSet>()

        VfsUtil.visitChildrenRecursively(
            projectRoot,
            object : VirtualFileVisitor<Unit>() {
                override fun visitFile(file: VirtualFile): Boolean {
                    if (!file.isDirectory) {
                        if (IGNORE_FILE_NAMES.contains(file.name)) {
                            val relativeBase = VfsUtil.getRelativePath(file.parent, projectRoot, '/') ?: ""
                            val patterns = readIgnoreFile(file)
                            if (patterns.isNotEmpty()) {
                                ruleSets.add(IgnoreRuleSet(relativeBase, patterns))
                            }
                        }
                        return true
                    }

                    return !shouldSkipDirectory(file.name)
                }
            },
        )

        logger.info("Loaded ${ruleSets.size} ignore rule sets for project ${project.name}")
        return ruleSets
    }

    private fun getProjectRoot(): VirtualFile? {
        project.baseDir?.let { return it }
        val basePath = project.basePath ?: return null
        return LocalFileSystem.getInstance().findFileByPath(basePath)
    }

    private fun readIgnoreFile(file: VirtualFile): List<String> {
        return try {
            val content = String(file.contentsToByteArray(), StandardCharsets.UTF_8)
            content.lineSequence()
                .map { it.trim() }
                .filter { it.isNotEmpty() && !it.startsWith("#") }
                .toList()
        } catch (exception: Exception) {
            logger.warn("Failed to read ignore file: ${file.path}", exception)
            emptyList()
        }
    }

    private fun buildGlobMatchers(): List<GlobMatcher> {
        val matchers = mutableListOf<GlobMatcher>()

        settingsService.getExcludedFilePatterns()
            .filter { it.contains('*') }
            .forEach { pattern ->
                matchers.add(GlobMatcher(pattern, directoryOnly = false))
            }

        settingsService.getExcludedExtensions()
            .filter { it.contains('*') }
            .forEach { pattern ->
                matchers.add(GlobMatcher("*.${pattern.removePrefix("*.")}", directoryOnly = false))
            }

        return matchers
    }

    companion object {
        private val IGNORE_FILE_NAMES = setOf(
            ".gitignore",
            ".cursorignore",
            ".aiderignore",
            ".copilotignore",
        )
    }
}

private class IgnoreRuleSet(
    basePath: String,
    patterns: List<String>,
) {
    private val normalizedBasePath: String = normalizePath(basePath)
    private val rules: List<IgnoreRule> = patterns.map { IgnoreRule.parse(it) }

    fun isIgnored(relativePath: String, isDirectory: Boolean): Boolean {
        val normalizedPath = normalizePath(relativePath)
        if (normalizedBasePath.isNotEmpty() &&
            normalizedPath != normalizedBasePath &&
            !normalizedPath.startsWith("$normalizedBasePath/")
        ) {
            return false
        }

        val pathInScope = when {
            normalizedBasePath.isEmpty() -> normalizedPath
            normalizedPath == normalizedBasePath -> ""
            else -> normalizedPath.removePrefix("$normalizedBasePath/")
        }

        if (pathInScope.isEmpty()) {
            return false
        }

        var ignored = false
        for (rule in rules) {
            if (rule.matches(pathInScope, isDirectory)) {
                ignored = !rule.negated
            }
        }
        return ignored
    }
}

private data class IgnoreRule(
    val pattern: String,
    val negated: Boolean,
    val directoryOnly: Boolean,
    val anchoredToBase: Boolean,
) {
    fun matches(path: String, isDirectory: Boolean): Boolean {
        if (directoryOnly && !isDirectory) {
            return false
        }

        val candidate = if (isDirectory && !path.endsWith('/')) "$path/" else path
        val regex = patternToRegex(pattern, anchoredToBase)
        return regex.matches(candidate) || regex.matches(path)
    }

    companion object {
        fun parse(rawPattern: String): IgnoreRule {
            var pattern = rawPattern.trim()
            val negated = pattern.startsWith("!")
            if (negated) {
                pattern = pattern.drop(1)
            }

            val directoryOnly = pattern.endsWith("/")
            if (directoryOnly) {
                pattern = pattern.dropLast(1)
            }

            val anchoredToBase = pattern.startsWith("/")
            if (anchoredToBase) {
                pattern = pattern.drop(1)
            }

            return IgnoreRule(
                pattern = pattern,
                negated = negated,
                directoryOnly = directoryOnly,
                anchoredToBase = anchoredToBase,
            )
        }

        private fun patternToRegex(pattern: String, anchoredToBase: Boolean): Regex {
            val builder = StringBuilder()
            if (!anchoredToBase) {
                builder.append("(^|/)")
            } else {
                builder.append("^")
            }

            var index = 0
            while (index < pattern.length) {
                when (val char = pattern[index]) {
                    '*' -> {
                        if (index + 1 < pattern.length && pattern[index + 1] == '*') {
                            builder.append(".*")
                            index++
                            if (index + 1 < pattern.length && pattern[index + 1] == '/') {
                                index++
                            }
                        } else {
                            builder.append("[^/]*")
                        }
                    }
                    '?' -> builder.append("[^/]")
                    '.' -> builder.append("\\.")
                    '\\' -> builder.append("\\\\")
                    else -> {
                        if ("[](){}+^$|".contains(char)) {
                            builder.append('\\')
                        }
                        builder.append(char)
                    }
                }
                index++
            }

            builder.append("($|/)")
            return builder.toString().toRegex()
        }
    }
}

private class GlobMatcher(
    pattern: String,
    private val directoryOnly: Boolean,
) {
    private val pathMatcher: PathMatcher = FileSystems.getDefault().getPathMatcher("glob:$pattern")

    fun matches(path: String, isDirectory: Boolean): Boolean {
        if (directoryOnly && !isDirectory) {
            return false
        }

        val fileName = path.substringAfterLast('/')
        return try {
            pathMatcher.matches(java.nio.file.Paths.get(path)) ||
                pathMatcher.matches(java.nio.file.Paths.get(fileName))
        } catch (_: Exception) {
            false
        }
    }
}

private fun normalizePath(path: String): String = path.replace('\\', '/').trim('/')
