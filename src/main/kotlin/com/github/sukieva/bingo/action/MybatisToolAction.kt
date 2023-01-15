package com.github.sukieva.bingo.action

import com.github.sukieva.bingo.infrastructure.BingoContext
import com.github.sukieva.bingo.ui.MybatisToolDialog
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.psi.PsiClass
import com.intellij.psi.util.PsiTreeUtil

/**
 * MybatisToolAction
 *
 * @author SukiEva
 * @since 2023/01/15
 */
class MybatisToolAction : DumbAwareAction() {
    private val log = Logger.getInstance(MybatisToolAction::class.java)

    override fun actionPerformed(event: AnActionEvent) {
        val psiFile = event.getData(CommonDataKeys.PSI_FILE)
        val psiClass = PsiTreeUtil.getChildOfType(psiFile, PsiClass::class.java)
        if (psiClass == null) {
            log.error("class is null")
            return
        }
        val bingoContext = BingoContext()
        bingoContext.project = event.project
        bingoContext.dialogTitle = "MybatisTool"
        bingoContext.psiClass = psiClass
        bingoContext.psiFile = psiFile
        val mybatisToolDialog = MybatisToolDialog(bingoContext)
        mybatisToolDialog.show()
    }

    override fun getActionUpdateThread() = ActionUpdateThread.BGT
}