package com.github.sukieva.bingo.util.psi

import com.intellij.psi.CommonClassNames
import com.intellij.psi.PsiArrayType
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiPrimitiveType
import com.intellij.psi.PsiType
import com.intellij.psi.PsiTypes
import com.intellij.psi.util.InheritanceUtil
import com.intellij.psi.util.PsiTypesUtil


/**
 * PsiType 工具封装
 *
 * @author Suki
 * @since 2024-02-17
 */
object BingoPsiTypeUtils {
    private val UNBOXED_TYPE_MAP = mapOf(
        CommonClassNames.JAVA_LANG_BYTE to PsiTypes.byteType(),
        CommonClassNames.JAVA_LANG_CHARACTER to PsiTypes.charType(),
        CommonClassNames.JAVA_LANG_DOUBLE to PsiTypes.doubleType(),
        CommonClassNames.JAVA_LANG_FLOAT to PsiTypes.floatType(),
        CommonClassNames.JAVA_LANG_INTEGER to PsiTypes.intType(),
        CommonClassNames.JAVA_LANG_LONG to PsiTypes.longType(),
        CommonClassNames.JAVA_LANG_SHORT to PsiTypes.shortType(),
        CommonClassNames.JAVA_LANG_BOOLEAN to PsiTypes.byteType(),
    )

    fun PsiType.getPsiClass(): PsiClass? = PsiTypesUtil.getPsiClass(this)

    fun PsiType.unboxedIfPossible(): PsiPrimitiveType? = UNBOXED_TYPE_MAP[this.canonicalText]

    fun PsiType.isPrimitiveType(): Boolean = this is PsiPrimitiveType

    fun PsiType.isArrayType(): Boolean = this is PsiArrayType

    fun PsiType.isClassType(): Boolean = this is PsiClassType

    fun PsiType.isEnumType(): Boolean = this.getPsiClass()?.isEnum ?: false

    fun PsiType.isBoxedType(): Boolean = UNBOXED_TYPE_MAP.containsKey(this.canonicalText)

    fun PsiType.isCollectionType(): Boolean {
        val psiClass = this.getPsiClass() ?: return false
        return InheritanceUtil.isInheritor(
            psiClass, CommonClassNames.JAVA_UTIL_COLLECTION
        ) || InheritanceUtil.isInheritor(psiClass, CommonClassNames.JAVA_UTIL_MAP)
    }
}