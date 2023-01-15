package com.github.sukieva.bingo.ui

import com.github.sukieva.bingo.infrastructure.BingoContext
import com.github.sukieva.bingo.infrastructure.annotation.Bingo
import com.github.sukieva.bingo.infrastructure.wrapper.MarkdownWrapper
import com.github.sukieva.bingo.toolbox.tool.MybatisTool
import com.intellij.openapi.ui.DialogPanel
import kotlin.reflect.KFunction


/**
 * MybatisToolDialog
 *
 * @author SukiEva
 * @since 2023/01/15
 */
class MybatisToolDialog(bingoContext: BingoContext) :
    BaseDialog(bingoContext) {
    override fun buildTagPages(): List<KFunction<DialogPanel>> {
        return listOf(
            ::selectPage,
            ::insertPage
        )
    }

    @Bingo("select", "select sql", false)
    fun selectPage(bingo: BingoContext): DialogPanel {
        val markdownText = MarkdownWrapper.wrapCode(MybatisTool.select(bingo.psiClass!!), "xml")
        return buildMarkdownPanel(bingo, markdownText)
    }

    @Bingo("insert", "insert sql", true)
    fun insertPage(bingo: BingoContext): DialogPanel {
        val markdownText = StringBuilder().apply {
            appendLine("### insert one")
            appendLine(MarkdownWrapper.wrapCode(MybatisTool.insertOne(bingo.psiClass!!), "xml"))
            appendLine("### insert list")
            append(MarkdownWrapper.wrapCode(MybatisTool.insertList(bingo.psiClass!!), "xml"))
        }.toString()
        return buildMarkdownPanel(bingo, markdownText)
    }
}