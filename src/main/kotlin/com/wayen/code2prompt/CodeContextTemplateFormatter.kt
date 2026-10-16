package com.wayen.code2prompt

import java.nio.file.Path

data class CodeContext(
    val fileName: String,
    val absolutePath: String,
    val startLine: Int,
    val endLine: Int,
    val location: String?,
    val code: String,
    val languageId: String,
)

object CodeContextTemplateFormatter {

    fun format(context: CodeContext): String = buildString {
        append('[')
        append(context.fileName)
        append("](")
        append(context.absolutePath)
        append(")  行号: ")
        append(lineRange(context))

        context.location?.takeIf { it.isNotBlank() }?.let {
            append(" 位置: ")
            append(it)
        }

        append(" 关键代码:\n```")
        append(codeFenceLanguage(context.languageId))
        append('\n')
        append(context.code)
        append("\n```\n\n")
    }

    fun formatHtml(context: CodeContext): String = buildString {
        val fileName = context.fileName.escapeHtml()
        val path = context.absolutePath.escapeHtml()
        val fileUri = Path.of(context.absolutePath).toUri().toASCIIString().escapeHtml()
        val language = when (val id = context.languageId.lowercase()) {
            "kt" -> "kotlin"
            else -> id
        }.escapeHtml()

        append("<html><body><p><a href=\"")
        append(fileUri)
        // Codex 将这些属性识别为本地文件引用；普通 file:// 链接会被其粘贴过滤器忽略。
        append("\"><span at-mention-label=\"")
        append(fileName)
        append("\" at-mention-path=\"")
        append(path)
        append("\" at-mention-fs-path=\"")
        append(path)
        append("\">")
        append(fileName)
        append("</span></a> 行号: ")
        append(lineRange(context))
        context.location?.takeIf { it.isNotBlank() }?.let {
            append(" 位置: ")
            append(it.escapeHtml())
        }
        append(" 关键代码:</p><pre><code class=\"language-")
        append(language)
        append("\">")
        append(context.code.escapeHtml())
        append("</code></pre><p></p><p></p></body></html>")
    }

    private fun String.escapeHtml(): String = buildString {
        for (character in this@escapeHtml) {
            append(
                when (character) {
                    '&' -> "&amp;"
                    '<' -> "&lt;"
                    '>' -> "&gt;"
                    '"' -> "&quot;"
                    '\'' -> "&#39;"
                    else -> character
                },
            )
        }
    }

    private fun lineRange(context: CodeContext): String =
        if (context.startLine == context.endLine) context.startLine.toString()
        else "${context.startLine}-${context.endLine}"

    private fun codeFenceLanguage(languageId: String): String = when (languageId.lowercase()) {
        "kotlin" -> "kt"
        "java" -> "java"
        "xml" -> "xml"
        "json" -> "json"
        else -> languageId.lowercase()
    }
}
