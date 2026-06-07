package com.aicontext.plugin.utils

object TreeRenderer {

    private class TreeNode(val name: String) {
        val children: MutableMap<String, TreeNode> = linkedMapOf()
        var isFile: Boolean = false
    }

    fun render(paths: List<String>): String {
        if (paths.isEmpty()) {
            return ""
        }

        val root = TreeNode("")
        for (path in paths) {
            val segments = path.split('/').filter { it.isNotEmpty() }
            if (segments.isEmpty()) continue

            var current = root
            segments.forEachIndexed { index, segment ->
                val isLastSegment = index == segments.lastIndex
                val child = current.children.getOrPut(segment) { TreeNode(segment) }
                if (isLastSegment) {
                    child.isFile = true
                }
                current = child
            }
        }

        return buildString {
            appendLine("Project Structure")
            appendLine()
            renderChildren(root, "", this)
        }.trimEnd()
    }

    private fun renderChildren(node: TreeNode, prefix: String, builder: StringBuilder) {
        val entries = node.children.entries.toList()
        entries.forEachIndexed { index, (name, child) ->
            val isLast = index == entries.lastIndex
            val connector = if (isLast) "└── " else "├── "
            builder.append(prefix).append(connector).append(name)
            if (!child.isFile && child.children.isNotEmpty()) {
                builder.append('/')
            }
            builder.appendLine()

            if (child.children.isNotEmpty()) {
                val childPrefix = prefix + if (isLast) "    " else "│   "
                renderChildren(child, childPrefix, builder)
            }
        }
    }
}
