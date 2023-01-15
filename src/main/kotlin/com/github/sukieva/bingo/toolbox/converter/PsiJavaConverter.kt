package com.github.sukieva.bingo.toolbox.converter

import com.github.sukieva.bingo.toolbox.vo.JavaFieldVO
import com.intellij.psi.PsiField

/**
 * Psi convert to Java
 *
 * @author SukiEva
 * @since 2023/01/15
 */
object PsiJavaConverter {
    fun toJavaField(psiField: PsiField): JavaFieldVO {
        val javaField = JavaFieldVO().apply {
            modifier = psiField.modifierList?.text ?: ""
            type = psiField.type.presentableText
            name = psiField.name
        }
        return javaField
    }
}