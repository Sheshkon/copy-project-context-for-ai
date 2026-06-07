package com.aicontext.plugin.services

import com.intellij.openapi.diagnostic.Logger
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

class ClipboardService {

    private val logger = Logger.getInstance(ClipboardService::class.java)

    fun copyToClipboard(text: String) {
        try {
            val selection = StringSelection(text)
            Toolkit.getDefaultToolkit().systemClipboard.setContents(selection, selection)
            logger.info("Copied ${text.length} characters to clipboard")
        } catch (exception: Exception) {
            logger.error("Failed to copy text to clipboard", exception)
            throw exception
        }
    }
}
