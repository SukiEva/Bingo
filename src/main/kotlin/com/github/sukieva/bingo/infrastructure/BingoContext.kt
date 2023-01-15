package com.github.sukieva.bingo.infrastructure

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiFile

/**
 * BingoContext
 *
 * @author SukiEva
 * @since 2023/01/15
 */
data class BingoContext(
    var project: Project? = null,
    var psiFile: PsiFile? = null,
    var psiClass: PsiClass? = null,
    var dialogTitle: String? = null
)

