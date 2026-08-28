package com.wayen.code2prompt

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
        append("\n```\n\n要做什么:")
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
