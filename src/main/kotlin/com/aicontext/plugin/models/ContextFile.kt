package com.aicontext.plugin.models

data class ContextFile(
    val relativePath: String,
    val content: String,
    val language: String,
)
