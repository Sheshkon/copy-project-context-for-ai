package com.aicontext.plugin.utils

object LanguageDetector {

    private val extensionToLanguage = mapOf(
        "java" to "java",
        "kt" to "kotlin",
        "kts" to "kotlin",
        "py" to "python",
        "js" to "javascript",
        "jsx" to "jsx",
        "ts" to "typescript",
        "tsx" to "tsx",
        "go" to "go",
        "rs" to "rust",
        "php" to "php",
        "rb" to "ruby",
        "cs" to "csharp",
        "cpp" to "cpp",
        "cc" to "cpp",
        "cxx" to "cpp",
        "c" to "c",
        "h" to "c",
        "hpp" to "cpp",
        "sql" to "sql",
        "xml" to "xml",
        "json" to "json",
        "yaml" to "yaml",
        "yml" to "yaml",
        "toml" to "toml",
        "html" to "html",
        "htm" to "html",
        "css" to "css",
        "scss" to "scss",
        "sass" to "scss",
        "less" to "css",
        "md" to "markdown",
        "markdown" to "markdown",
        "sh" to "bash",
        "bash" to "bash",
        "zsh" to "bash",
        "dockerfile" to "dockerfile",
        "gradle" to "gradle",
        "groovy" to "groovy",
        "properties" to "properties",
        "ini" to "ini",
        "cfg" to "ini",
        "conf" to "ini",
        "vue" to "vue",
        "svelte" to "svelte",
        "swift" to "swift",
        "m" to "objectivec",
        "mm" to "objectivec",
        "r" to "r",
        "lua" to "lua",
        "pl" to "perl",
        "pm" to "perl",
        "ex" to "elixir",
        "exs" to "elixir",
        "erl" to "erlang",
        "hrl" to "erlang",
        "clj" to "clojure",
        "cljs" to "clojure",
        "scala" to "scala",
        "sc" to "scala",
        "tf" to "hcl",
        "hcl" to "hcl",
        "proto" to "protobuf",
        "bat" to "batch",
        "cmd" to "batch",
        "ps1" to "powershell",
        "psm1" to "powershell",
    )

    fun detect(fileName: String): String {
        val lowerName = fileName.lowercase()
        if (lowerName == "dockerfile" || lowerName.startsWith("dockerfile.")) {
            return "dockerfile"
        }
        if (lowerName == "makefile" || lowerName == "gnumakefile") {
            return "makefile"
        }
        val extension = lowerName.substringAfterLast('.', "")
        if (extension.isEmpty() || extension == lowerName) {
            return "text"
        }
        return extensionToLanguage[extension] ?: "text"
    }
}
