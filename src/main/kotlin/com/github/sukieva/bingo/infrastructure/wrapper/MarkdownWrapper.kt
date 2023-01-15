package com.github.sukieva.bingo.infrastructure.wrapper

/**
 * MarkdownUtil
 *
 * @author SukiEva
 * @since 2023/01/15
 */
object MarkdownWrapper {
     fun wrapCode(content: String, codeType : String): String {
        return StringBuilder().apply {
            append("```").append(codeType).appendLine()
            append(content)
            append("```")
        }.toString()
    }
}