package com.aicontext.plugin.actions

import com.aicontext.plugin.services.ClipboardService
import com.aicontext.plugin.services.ContextFormatterService
import com.aicontext.plugin.services.FileCollectorService
import com.aicontext.plugin.services.IgnoreRulesService
import com.aicontext.plugin.services.SettingsService
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile

class CopyProjectContextAction : AnAction() {

    private val logger = Logger.getInstance(CopyProjectContextAction::class.java)

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(event: AnActionEvent) {
        val project = event.project
        val selectedFiles = event.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)
        val visible = project != null
        val enabled = visible && !selectedFiles.isNullOrEmpty()
        event.presentation.isVisible = visible
        event.presentation.isEnabled = enabled
    }

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val selectedFiles = event.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY) ?: return

        ProgressManager.getInstance().run(
            object : Task.Backgroundable(project, "Copy Project Context for AI", true) {
                override fun run(indicator: ProgressIndicator) {
                    try {
                        copyProjectContext(project, selectedFiles, indicator)
                    } catch (exception: Exception) {
                        logger.error("Failed to generate AI context", exception)
                        showFailureNotification(project, exception)
                    }
                }
            },
        )
    }

    private fun copyProjectContext(
        project: com.intellij.openapi.project.Project,
        selectedFiles: Array<VirtualFile>,
        indicator: ProgressIndicator,
    ) {
        indicator.isIndeterminate = false
        indicator.text = "Preparing ignore rules..."

        val settingsService = SettingsService.getInstance()
        val ignoreRulesService = IgnoreRulesService(project, settingsService)
        val fileCollectorService = FileCollectorService(project, ignoreRulesService, settingsService)
        val contextFormatterService = ContextFormatterService(settingsService)
        val clipboardService = ClipboardService()

        indicator.fraction = 0.1
        indicator.text = "Counting eligible files..."

        val eligibleFileCount = fileCollectorService.countEligibleFiles(selectedFiles)
        if (eligibleFileCount == 0) {
            ApplicationManager.getApplication().invokeLater {
                Messages.showWarningDialog(
                    project,
                    "No eligible files were found in the current selection.",
                    "Copy Project Context for AI",
                )
            }
            return
        }

        if (eligibleFileCount > settingsService.getMaxFiles()) {
            val shouldContinue = askToContinue(
                project,
                "The selection contains $eligibleFileCount files, which exceeds the configured limit of ${settingsService.getMaxFiles()} files.\n\nDo you want to continue anyway?",
            )
            if (!shouldContinue) {
                return
            }
        }

        indicator.fraction = 0.3
        indicator.text = "Collecting files..."

        val collectionResult = fileCollectorService.collect(selectedFiles)
        if (collectionResult.files.isEmpty()) {
            ApplicationManager.getApplication().invokeLater {
                Messages.showWarningDialog(
                    project,
                    "No eligible files were found in the current selection.",
                    "Copy Project Context for AI",
                )
            }
            return
        }

        indicator.fraction = 0.6
        indicator.text = "Formatting context..."

        val formattedContext = contextFormatterService.format(collectionResult.files)
        val characterCount = formattedContext.length

        if (characterCount > settingsService.getMaxCharacters()) {
            val shouldContinue = askToContinue(
                project,
                "The generated context contains $characterCount characters, which exceeds the configured limit of ${settingsService.getMaxCharacters()} characters.\n\nDo you want to continue anyway?",
            )
            if (!shouldContinue) {
                return
            }
        }

        indicator.fraction = 0.9
        indicator.text = "Copying to clipboard..."

        clipboardService.copyToClipboard(formattedContext)

        indicator.fraction = 1.0
        showSuccessNotification(project, collectionResult.files.size, characterCount, collectionResult.skippedLargeFiles)
    }

    private fun askToContinue(project: com.intellij.openapi.project.Project, message: String): Boolean {
        val result = booleanArrayOf(false)
        ApplicationManager.getApplication().invokeAndWait {
            result[0] = Messages.showYesNoDialog(
                project,
                message,
                "Large Project Warning",
                "Continue",
                "Cancel",
                Messages.getWarningIcon(),
            ) == Messages.YES
        }
        return result[0]
    }

    private fun showSuccessNotification(
        project: com.intellij.openapi.project.Project,
        fileCount: Int,
        characterCount: Int,
        skippedLargeFiles: Int,
    ) {
        val content = buildString {
            append("Files: $fileCount\n")
            append("Characters: $characterCount")
            if (skippedLargeFiles > 0) {
                append("\nSkipped large files: $skippedLargeFiles")
            }
        }

        ApplicationManager.getApplication().invokeLater {
            Notifications.Bus.notify(
                Notification(
                    NOTIFICATION_GROUP_ID,
                    "AI context copied successfully",
                    content,
                    NotificationType.INFORMATION,
                ),
                project,
            )
        }
    }

    private fun showFailureNotification(project: com.intellij.openapi.project.Project, exception: Exception) {
        ApplicationManager.getApplication().invokeLater {
            Notifications.Bus.notify(
                Notification(
                    NOTIFICATION_GROUP_ID,
                    "Failed to generate AI context",
                    exception.message ?: exception.javaClass.simpleName,
                    NotificationType.ERROR,
                ),
                project,
            )
        }
    }

    companion object {
        private const val NOTIFICATION_GROUP_ID = "Copy Project Context for AI"
    }
}
