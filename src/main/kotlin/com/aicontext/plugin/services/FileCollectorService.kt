package com.aicontext.plugin.services

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VFileProperty
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.aicontext.plugin.models.ContextFile
import com.aicontext.plugin.utils.LanguageDetector
import java.nio.charset.StandardCharsets
import java.util.TreeSet

class FileCollectorService(
    private val project: Project,
    private val ignoreRulesService: IgnoreRulesService,
    private val settingsService: SettingsService,
) {
    private val logger = Logger.getInstance(FileCollectorService::class.java)

    data class CollectionResult(
        val files: List<ContextFile>,
        val skippedLargeFiles: Int,
        val totalCharacterCount: Int,
    )

    fun collect(selectedFiles: Array<VirtualFile>): CollectionResult {
        val projectRoot = getProjectRoot(project) ?: throw IllegalStateException("Project root is not available")
        val collectedPaths = TreeSet<String>()
        val contextFiles = mutableListOf<ContextFile>()
        var skippedLargeFiles = 0
        var totalCharacterCount = 0

        for (selected in selectedFiles) {
            if (!VfsUtil.isAncestor(projectRoot, selected, false) && selected != projectRoot) {
                logger.warn("Skipping selection outside project root: ${selected.path}")
                continue
            }
            collectFromVirtualFile(projectRoot, selected, collectedPaths, contextFiles) { skipped ->
                skippedLargeFiles += skipped
            }
        }

        for (contextFile in contextFiles) {
            totalCharacterCount += contextFile.content.length
        }

        return CollectionResult(
            files = contextFiles,
            skippedLargeFiles = skippedLargeFiles,
            totalCharacterCount = totalCharacterCount,
        )
    }

    fun countEligibleFiles(selectedFiles: Array<VirtualFile>): Int {
        val projectRoot = getProjectRoot(project) ?: return 0
        val collectedPaths = TreeSet<String>()

        for (selected in selectedFiles) {
            if (!VfsUtil.isAncestor(projectRoot, selected, false) && selected != projectRoot) {
                continue
            }
            countFromVirtualFile(projectRoot, selected, collectedPaths)
        }

        return collectedPaths.size
    }

    private fun collectFromVirtualFile(
        projectRoot: VirtualFile,
        file: VirtualFile,
        collectedPaths: MutableSet<String>,
        contextFiles: MutableList<ContextFile>,
        onSkippedLargeFile: (Int) -> Unit,
    ) {
        val relativePath = getRelativePath(projectRoot, file) ?: return

        if (ignoreRulesService.isIgnored(relativePath, file.isDirectory)) {
            return
        }

        if (file.isDirectory) {
            if (ignoreRulesService.shouldSkipDirectory(file.name)) {
                return
            }

            for (child in file.children) {
                collectFromVirtualFile(projectRoot, child, collectedPaths, contextFiles, onSkippedLargeFile)
            }
            return
        }

        if (!file.isValid || file.`is`(VFileProperty.SYMLINK) || collectedPaths.contains(relativePath)) {
            return
        }

        collectedPaths.add(relativePath)

        if (!settingsService.isIncludeFileContents()) {
            contextFiles.add(
                ContextFile(
                    relativePath = relativePath,
                    content = "",
                    language = LanguageDetector.detect(file.name),
                ),
            )
            return
        }

        if (file.length > settingsService.getMaxSingleFileSize()) {
            logger.info("Skipping large file: $relativePath (${file.length} bytes)")
            onSkippedLargeFile(1)
            collectedPaths.remove(relativePath)
            return
        }

        val content = readFileContent(file)
        contextFiles.add(
            ContextFile(
                relativePath = relativePath,
                content = content,
                language = LanguageDetector.detect(file.name),
            ),
        )
    }

    private fun countFromVirtualFile(
        projectRoot: VirtualFile,
        file: VirtualFile,
        collectedPaths: MutableSet<String>,
    ) {
        val relativePath = getRelativePath(projectRoot, file) ?: return

        if (ignoreRulesService.isIgnored(relativePath, file.isDirectory)) {
            return
        }

        if (file.isDirectory) {
            if (ignoreRulesService.shouldSkipDirectory(file.name)) {
                return
            }

            for (child in file.children) {
                countFromVirtualFile(projectRoot, child, collectedPaths)
            }
            return
        }

        if (!file.isValid || file.`is`(VFileProperty.SYMLINK)) {
            return
        }

        collectedPaths.add(relativePath)
    }

    private fun getRelativePath(projectRoot: VirtualFile, file: VirtualFile): String? {
        return VfsUtil.getRelativePath(file, projectRoot, '/')?.replace('\\', '/')
    }

    private fun getProjectRoot(project: Project): VirtualFile? {
        project.baseDir?.let { return it }
        val basePath = project.basePath ?: return null
        return LocalFileSystem.getInstance().findFileByPath(basePath)
    }

    private fun readFileContent(file: VirtualFile): String {
        return try {
            val bytes = file.contentsToByteArray()
            String(bytes, StandardCharsets.UTF_8)
        } catch (exception: Exception) {
            logger.warn("Failed to read file content: ${file.path}", exception)
            "[Failed to read file content: ${exception.message}]"
        }
    }
}
