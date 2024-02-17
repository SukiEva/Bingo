package com.github.sukieva.bingo.util.psi

import com.intellij.psi.*
import com.intellij.psi.util.InheritanceUtil
import com.intellij.psi.util.PsiUtil


/**
 * PsiType 工具封装
 *
 * @author Suki
 * @since 2024-02-17
 */
object BingoPsiTypeUtils {
    private val UNBOXED_TYPE_MAP = mapOf(
        CommonClassNames.JAVA_LANG_BYTE to PsiType.BYTE,
        CommonClassNames.JAVA_LANG_CHARACTER to PsiType.CHAR,
        CommonClassNames.JAVA_LANG_DOUBLE to PsiType.DOUBLE,
        CommonClassNames.JAVA_LANG_FLOAT to PsiType.FLOAT,
        CommonClassNames.JAVA_LANG_INTEGER to PsiType.INT,
        CommonClassNames.JAVA_LANG_LONG to PsiType.LONG,
        CommonClassNames.JAVA_LANG_SHORT to PsiType.SHORT,
        CommonClassNames.JAVA_LANG_BOOLEAN to PsiType.BOOLEAN,
    )

    fun unboxedIfPossible(type: PsiType): PsiPrimitiveType? = UNBOXED_TYPE_MAP[type.canonicalText]

    fun isPrimitiveType(type: PsiType): Boolean = type is PsiPrimitiveType

    fun isArrayType(type: PsiType): Boolean = type is PsiArrayType

    fun isClassType(type: PsiType): Boolean = type is PsiClassType

    fun isEnumType(type: PsiType): Boolean = PsiUtil.resolveClassInClassTypeOnly(type)?.isEnum ?: false

    fun isBoxedType(type: PsiType): Boolean = UNBOXED_TYPE_MAP.containsKey(type.canonicalText)

    fun isCollectionType(type: PsiType): Boolean {
        val psiClass = PsiUtil.resolveClassInClassTypeOnly(type) ?: return false
        return InheritanceUtil.isInheritor(
            psiClass, CommonClassNames.JAVA_UTIL_COLLECTION
        ) || InheritanceUtil.isInheritor(psiClass, CommonClassNames.JAVA_UTIL_MAP)
    }
}