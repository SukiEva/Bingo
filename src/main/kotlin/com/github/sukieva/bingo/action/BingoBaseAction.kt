package com.github.sukieva.bingo.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.util.PsiTreeUtil

/**
 * Action 封装
 *
 * @author Suki
 * @since 2024-02-17
 */
abstract class BingoBaseAction : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val editor = event.getData(CommonDataKeys.EDITOR)
        val project = event.project
        if (editor == null || project == null) {
            return
        }
        val psiFile = event.getData(CommonDataKeys.PSI_FILE)
        psiFile?.let {
            val psiClass = PsiTreeUtil.getChildOfType(it, PsiClass::class.java)
            psiClass?.let { clazz ->
                performAction(event, project, editor, clazz)
            }
        }
    }

    abstract fun performAction(
        event: AnActionEvent,
        project: Project,
        editor: Editor,
        psiClass: PsiClass
    )
}