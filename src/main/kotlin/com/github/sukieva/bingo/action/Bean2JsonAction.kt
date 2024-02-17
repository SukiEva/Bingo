package com.github.sukieva.bingo.action

import com.github.sukieva.bingo.constant.FastjsonConstants
import com.github.sukieva.bingo.constant.JacksonConstants
import com.github.sukieva.bingo.util.psi.BingoPsiTypeUtils
import com.google.gson.GsonBuilder
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.CommonClassNames
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiEnumConstant
import com.intellij.psi.PsiField
import com.intellij.psi.PsiType
import com.intellij.psi.util.PsiTypesUtil
import com.intellij.psi.util.PsiUtil
import com.intellij.util.containers.stream
import org.apache.commons.lang3.StringUtils
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime


/**
 * Java Bean 转 Json
 *
 * @author Suki
 * @since 2024-02-17
 */
class Bean2JsonAction : BingoBaseAction() {
    companion object {
        // 支持 Jackson 和 FastJson 注解
        private val JSON_NAME_MAP = linkedMapOf(
            JacksonConstants.JSON_PROPERTY to JacksonConstants.JSON_PROPERTY_NAME,
            FastjsonConstants.JSON_FIELD_2 to FastjsonConstants.JSON_FIELD_NAME,
            FastjsonConstants.JSON_FIELD_1 to FastjsonConstants.JSON_FIELD_NAME
        ).toMap()

        // 支持 Java 常用类型
        private val COMMON_CLASS_MAP = mapOf(
            CommonClassNames.JAVA_LANG_STRING to StringUtils.EMPTY,
            CommonClassNames.JAVA_TIME_LOCAL_DATE to LocalDate.now(),
            CommonClassNames.JAVA_TIME_LOCAL_TIME to LocalTime.now(),
            CommonClassNames.JAVA_UTIL_DATE to LocalDateTime.now(),
            CommonClassNames.JAVA_TIME_LOCAL_DATE_TIME to LocalDateTime.now(),
            "java.math.BigDecimal" to 0.0,
            "java.math.BigInteger" to 0
        ).toMap()
    }

    override fun performAction(event: AnActionEvent, project: Project, editor: Editor, psiClass: PsiClass) {
        val jsonMap = getDefaultValueOfClass(psiClass, mutableSetOf())
        val jsonStr = GsonBuilder().setPrettyPrinting().create().toJson(jsonMap)
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        val stringSelection = StringSelection(jsonStr)
        clipboard.setContents(stringSelection, null)
    }

    private fun getDefaultValueOfClass(psiClass: PsiClass?, existsTypeSet: MutableSet<String>): Map<String, Any?> {
        if (psiClass == null) return emptyMap()
        val jsonMap = mutableMapOf<String, Any?>()
        existsTypeSet.add(getQualifiedName(psiClass))
        psiClass.allFields.forEach { psiField ->
            val jsonName = getJsonName(psiField)
            val psiType = psiField.type
            val jsonValue = getDefaultValueOfType(psiType, existsTypeSet.toMutableSet())
            jsonMap[jsonName] = jsonValue
        }
        return jsonMap
    }

    private fun getDefaultValueOfType(type: PsiType, existsTypeSet: MutableSet<String>): Any? = when {
        // 基本类型
        BingoPsiTypeUtils.isPrimitiveType(type) -> PsiTypesUtil.getDefaultValue(type)
        // 包装类型
        BingoPsiTypeUtils.isBoxedType(type) -> PsiTypesUtil.getDefaultValue(BingoPsiTypeUtils.unboxedIfPossible(type))
        // 数组类型
        BingoPsiTypeUtils.isArrayType(type) -> {
            val elementType = type.deepComponentType
            getDefaultValueOfElementType(existsTypeSet, elementType)
        }
        // 集合类型
        BingoPsiTypeUtils.isCollectionType(type) -> {
            val elementType = PsiUtil.extractIterableTypeParameter(type, false)
            getDefaultValueOfElementType(existsTypeSet, elementType)
        }
        // 枚举类型
        BingoPsiTypeUtils.isEnumType(type) -> {
            val psiClass = PsiUtil.resolveClassInClassTypeOnly(type)
            psiClass?.fields.stream().filter { it is PsiEnumConstant }.map(PsiField::getName).toList()
        }
        // 常见类型
        COMMON_CLASS_MAP.containsKey(type.canonicalText) -> COMMON_CLASS_MAP[type.canonicalText]
        // 其他引用类型
        BingoPsiTypeUtils.isClassType(type) -> {
            val psiClass = PsiUtil.resolveClassInClassTypeOnly(type)
            val qualifiedName = getQualifiedName(psiClass)
            if (existsTypeSet.contains(qualifiedName)) {
                null
            } else {
                existsTypeSet.add(qualifiedName)
                getDefaultValueOfClass(psiClass, existsTypeSet.toMutableSet())
            }

        }

        else -> null
    }


    private fun getDefaultValueOfElementType(existsTypeSet: MutableSet<String>, elementType: PsiType?) =
        if (elementType == null || existsTypeSet.contains(elementType.canonicalText)) {
            emptyList()
        } else {
            val defaultValueOfType = getDefaultValueOfType(elementType, existsTypeSet.toMutableSet())
            defaultValueOfType?.let { listOf(it) }.orEmpty()
        }

    private fun getQualifiedName(psiClass: PsiClass?) = psiClass?.qualifiedName ?: StringUtils.EMPTY

    private fun getJsonName(psiField: PsiField): String {
        for ((fqn, attributeName) in JSON_NAME_MAP) {
            val jsonKeyByAnnotation = getJsonNameByAnnotation(psiField, fqn, attributeName)
            if (jsonKeyByAnnotation != null) {
                return jsonKeyByAnnotation
            }
        }
        return psiField.name
    }

    private fun getJsonNameByAnnotation(psiField: PsiField, fqn: String, attributeName: String): String? {
        return psiField.getAnnotation(fqn)?.let { psiAnnotation ->
            return psiAnnotation.findAttributeValue(attributeName)?.text?.removeSurrounding("\"")
        }
    }
}