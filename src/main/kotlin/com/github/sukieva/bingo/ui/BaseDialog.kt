package com.github.sukieva.bingo.ui

import com.github.sukieva.bingo.infrastructure.BingoContext
import com.github.sukieva.bingo.infrastructure.annotation.Bingo
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBTabbedPane
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.BottomGap
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.ui.JBEmptyBorder
import org.apache.commons.lang3.StringUtils
import org.intellij.plugins.markdown.settings.MarkdownSettings
import org.intellij.plugins.markdown.ui.preview.MarkdownHtmlPanelProvider
import org.intellij.plugins.markdown.ui.preview.html.MarkdownUtil
import java.awt.Dimension
import javax.swing.JComponent
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation

/**
 * BaseDialog
 *
 * @author SukiEva
 * @since 2023/01/15
 */
abstract class BaseDialog(private val bingoContext: BingoContext) :
    DialogWrapper(bingoContext.project, null, true, IdeModalityType.MODELESS, false) {
    companion object {
        val markdownProvider = MarkdownHtmlPanelProvider.createFromInfo(MarkdownSettings.defaultProviderInfo)
    }

    init {
        title = bingoContext.dialogTitle
        super.init()
    }

    abstract fun buildTagPages(): List<KFunction<DialogPanel>>

    override fun createCenterPanel(): JComponent {
        val jbTabbedPane = JBTabbedPane()
        jbTabbedPane.minimumSize = Dimension(400, 300)
        jbTabbedPane.preferredSize = Dimension(800, 600)
        buildTagPages().forEach { addTagPage(it, jbTabbedPane) }
        return jbTabbedPane
    }

    private fun addTagPage(bingo: KFunction<DialogPanel>, tabbedPane: JBTabbedPane) {
        val annotation = bingo.findAnnotation<Bingo>() ?: return

        val content = panel {
            if (!StringUtils.isEmpty(annotation.description)) {
                row {
                    label("<html>Description: ${annotation.description}")
                }.bottomGap(BottomGap.MEDIUM)
            }

            // set method parameters
            val args = bingo.parameters.associateBy(
                { it },
                {
                    when (it.name) {
                        "bingo" -> bingoContext
                        else -> null
                    }
                }
            )
            val panel = bingo.callBy(args)

            if (annotation.scrollbar) {
                row {
                    panel.border = JBEmptyBorder(10)
                    scrollCell(panel)
                        .align(AlignX.FILL)
                        .resizableColumn()
                }.resizableRow()
            } else {
                row {
                    cell(panel)
                        .align(AlignX.FILL)
                        .resizableColumn()
                }
            }
        }
        tabbedPane.add(annotation.title, content)
    }

    fun buildMarkdownPanel(bingo: BingoContext, markdownText: String): DialogPanel {
        val markdownPanel = markdownProvider.createHtmlPanel()
        markdownPanel.setHtml(
            MarkdownUtil.generateMarkdownHtml(
                bingo.psiFile!!.virtualFile,
                markdownText,
                bingo.project
            ), 0
        )
        return panel {
            row {
                cell(markdownPanel.component)
            }
        }
    }
}