package com.github.sukieva.bingo.action

import com.github.sukieva.bingo.constant.FastjsonConstants
import com.github.sukieva.bingo.constant.JacksonConstants
import com.github.sukieva.bingo.util.psi.BingoPsiTypeUtils.isArrayType
import com.github.sukieva.bingo.util.psi.BingoPsiTypeUtils.isBoxedType
import com.github.sukieva.bingo.util.psi.BingoPsiTypeUtils.isClassType
import com.github.sukieva.bingo.util.psi.BingoPsiTypeUtils.isCollectionType
import com.github.sukieva.bingo.util.psi.BingoPsiTypeUtils.isEnumType
import com.github.sukieva.bingo.util.psi.BingoPsiTypeUtils.isPrimitiveType
import com.github.sukieva.bingo.util.psi.BingoPsiTypeUtils.unboxedIfPossible
import com.github.sukieva.bingo.util.ui.ClipboardUtils
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileTypes.FileTypeManager
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
        val defaultValueMap = getDefaultValueOfClass(psiClass, mutableSetOf(getQualifiedName(psiClass)))
        ClipboardUtils.setJson(defaultValueMap)
    }

    override fun update(event: AnActionEvent) {
        val virtualFile = event.getData(CommonDataKeys.VIRTUAL_FILE)
        if (virtualFile == null) {
            event.presentation.isVisible = false
            return
        }
        val fileTypeManager = FileTypeManager.getInstance()
        event.presentation.isVisible =
            fileTypeManager.getFileTypeByFile(virtualFile) == fileTypeManager.getFileTypeByExtension("java")
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    private fun getDefaultValueOfClass(psiClass: PsiClass?, existsTypeSet: MutableSet<String>): Map<String, Any?> {
        if (psiClass == null) return emptyMap()
        val jsonMap = mutableMapOf<String, Any?>()
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
        type.isPrimitiveType() -> PsiTypesUtil.getDefaultValue(type)
        // 包装类型
        type.isBoxedType() -> PsiTypesUtil.getDefaultValue(type.unboxedIfPossible())
        // 数组类型
        type.isArrayType() -> {
            val elementType = type.deepComponentType
            getDefaultValueOfElementType(existsTypeSet, elementType)
        }
        // 集合类型
        type.isCollectionType() -> {
            val elementType = PsiUtil.extractIterableTypeParameter(type, false)
            getDefaultValueOfElementType(existsTypeSet, elementType)
        }
        // 枚举类型
        type.isEnumType() -> {
            val psiClass = PsiUtil.resolveClassInClassTypeOnly(type)
            psiClass?.fields.stream().filter { it is PsiEnumConstant }.map(PsiField::getName).toList()
        }
        // 常见类型
        COMMON_CLASS_MAP.containsKey(type.canonicalText) -> COMMON_CLASS_MAP[type.canonicalText]
        // 其他引用类型
        type.isClassType() -> {
            val psiClass = PsiUtil.resolveClassInClassTypeOnly(type)
            val qualifiedName = getQualifiedName(psiClass)
            if (existsTypeSet.contains(qualifiedName)) {
                null
            } else {
                existsTypeSet.add(qualifiedName)
                getDefaultValueOfClass(psiClass, existsTypeSet)
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