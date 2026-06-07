package com.aicontext.plugin.settings

import com.aicontext.plugin.services.SettingsService
import com.intellij.openapi.options.Configurable
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.JBUI
import javax.swing.JComponent
import javax.swing.JPanel

class PluginSettingsConfigurable : Configurable {

    private var panel: JPanel? = null

    private val maxFilesField = JBTextField()
    private val maxCharactersField = JBTextField()
    private val maxSingleFileSizeField = JBTextField()
    private val excludedDirectoriesArea = JBTextArea()
    private val excludedExtensionsArea = JBTextArea()
    private val includeProjectTreeCheckBox = JBCheckBox("Include project tree")
    private val includeFileContentsCheckBox = JBCheckBox("Include file contents")

    override fun getDisplayName(): String = "Copy Project Context for AI"

    override fun createComponent(): JComponent {
        excludedDirectoriesArea.rows = 8
        excludedExtensionsArea.rows = 8

        panel = FormBuilder.createFormBuilder()
            .addLabeledComponent(JBLabel("Maximum files:"), maxFilesField, 1, false)
            .addLabeledComponent(JBLabel("Maximum characters:"), maxCharactersField, 1, false)
            .addLabeledComponent(JBLabel("Maximum single file size (bytes):"), maxSingleFileSizeField, 1, false)
            .addLabeledComponent(
                JBLabel("Excluded directories (one per line):"),
                JBScrollPane(excludedDirectoriesArea),
                1,
                true,
            )
            .addLabeledComponent(
                JBLabel("Excluded extensions (one per line, without leading dot):"),
                JBScrollPane(excludedExtensionsArea),
                1,
                true,
            )
            .addComponent(includeProjectTreeCheckBox)
            .addComponent(includeFileContentsCheckBox)
            .addComponentFillVertically(JPanel(), 0)
            .panel

        panel?.border = JBUI.Borders.empty(10)
        reset()
        return panel!!
    }

    override fun isModified(): Boolean {
        val settingsService = SettingsService.getInstance()
        return maxFilesField.text.toIntOrNull() != settingsService.getMaxFiles() ||
            maxCharactersField.text.toIntOrNull() != settingsService.getMaxCharacters() ||
            maxSingleFileSizeField.text.toIntOrNull() != settingsService.getMaxSingleFileSize() ||
            excludedDirectoriesArea.text.lines().filter { it.isNotBlank() }.toSet() != settingsService.getExcludedDirectories() ||
            excludedExtensionsArea.text.lines().filter { it.isNotBlank() }.map { it.trim().lowercase().removePrefix(".") }.toSet() != settingsService.getExcludedExtensions() ||
            includeProjectTreeCheckBox.isSelected != settingsService.isIncludeProjectTree() ||
            includeFileContentsCheckBox.isSelected != settingsService.isIncludeFileContents()
    }

    override fun apply() {
        val settingsService = SettingsService.getInstance()
        settingsService.updateSettings(
            maxFiles = maxFilesField.text.toIntOrNull() ?: SettingsService.DEFAULT_MAX_FILES,
            maxCharacters = maxCharactersField.text.toIntOrNull() ?: SettingsService.DEFAULT_MAX_CHARACTERS,
            maxSingleFileSize = maxSingleFileSizeField.text.toIntOrNull() ?: SettingsService.DEFAULT_MAX_SINGLE_FILE_SIZE,
            excludedDirectories = excludedDirectoriesArea.text.lines(),
            excludedExtensions = excludedExtensionsArea.text.lines(),
            excludedFilePatterns = settingsService.getExcludedFilePatterns().toList(),
            includeProjectTree = includeProjectTreeCheckBox.isSelected,
            includeFileContents = includeFileContentsCheckBox.isSelected,
        )
    }

    override fun reset() {
        val settingsService = SettingsService.getInstance()
        maxFilesField.text = settingsService.getMaxFiles().toString()
        maxCharactersField.text = settingsService.getMaxCharacters().toString()
        maxSingleFileSizeField.text = settingsService.getMaxSingleFileSize().toString()
        excludedDirectoriesArea.text = settingsService.getExcludedDirectories().sorted().joinToString("\n")
        excludedExtensionsArea.text = settingsService.getExcludedExtensions().sorted().joinToString("\n")
        includeProjectTreeCheckBox.isSelected = settingsService.isIncludeProjectTree()
        includeFileContentsCheckBox.isSelected = settingsService.isIncludeFileContents()
    }

    override fun disposeUIResources() {
        panel = null
    }
}
