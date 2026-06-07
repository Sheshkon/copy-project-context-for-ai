package com.aicontext.plugin.services

import com.aicontext.plugin.models.ContextFile
import com.aicontext.plugin.utils.TreeRenderer

class ContextFormatterService(
    private val settingsService: SettingsService,
) {
    fun format(files: List<ContextFile>): String {
        if (files.isEmpty()) {
            return ""
        }

        return buildString {
            if (settingsService.isIncludeProjectTree()) {
                val paths = files.map { it.relativePath }
                append(TreeRenderer.render(paths))
                appendLine()
                appendLine()
            }

            if (settingsService.isIncludeFileContents()) {
                files.forEachIndexed { index, file ->
                    if (index > 0) {
                        appendLine()
                    }
                    append(formatFileSection(file))
                }
            } else {
                appendLine("Files")
                files.forEach { file ->
                    appendLine("- ${file.relativePath}")
                }
            }
        }.trimEnd()
    }

    fun estimateCharacterCount(files: List<ContextFile>): Int {
        if (files.isEmpty()) {
            return 0
        }

        var count = 0
        if (settingsService.isIncludeProjectTree()) {
            count += TreeRenderer.render(files.map { it.relativePath }).length + 2
        }

        if (settingsService.isIncludeFileContents()) {
            files.forEach { file ->
                count += formatFileSection(file).length + 1
            }
        } else {
            count += "Files\n".length
            files.forEach { file ->
                count += "- ${file.relativePath}\n".length
            }
        }

        return count
    }

    private fun formatFileSection(file: ContextFile): String {
        return buildString {
            appendLine(FILE_SEPARATOR)
            appendLine("FILE: ${file.relativePath}")
            appendLine(FILE_SEPARATOR)
            appendLine()
            append("```${file.language}")
            appendLine()
            append(file.content)
            if (file.content.isNotEmpty() && !file.content.endsWith("\n")) {
                appendLine()
            }
            append("```")
        }
    }

    companion object {
        private const val FILE_SEPARATOR = "=================================================="
    }
}
