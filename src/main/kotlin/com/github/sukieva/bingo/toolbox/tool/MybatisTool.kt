package com.github.sukieva.bingo.toolbox.tool

import com.github.sukieva.bingo.toolbox.converter.JdbcTypeConverter
import com.github.sukieva.bingo.toolbox.converter.PsiJavaConverter
import com.github.sukieva.bingo.toolbox.vo.JavaFieldVO
import com.google.common.base.CaseFormat
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiField
import com.intellij.psi.util.PsiTreeUtil

/**
 * MybatisTool
 *
 * @author SukiEva
 * @since 2023/01/15
 */
object MybatisTool {
    fun select(psiClass: PsiClass): String {
        val javaFields = getJavaFields(psiClass)
        val tableName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, psiClass.name ?: "table")
        return StringBuilder().apply {
            appendLine("select ${lowerUnderscoreFields(javaFields)}")
            appendLine("from $tableName")
        }.toString()
    }

    fun insertOne(psiClass: PsiClass): String {
        val javaFields = getJavaFields(psiClass)
        val tableName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, psiClass.name ?: "table")
        return StringBuilder().apply {
            appendLine("insert into $tableName")
            appendLine("(${lowerUnderscoreFields(javaFields)})")
            appendLine("values")
            appendLine("(${transferFields(javaFields)})")
        }.toString()
    }

    fun insertList(psiClass: PsiClass): String {
        val javaFields = getJavaFields(psiClass)
        val tableName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, psiClass.name ?: "table")
        return StringBuilder().apply {
            appendLine("insert into $tableName")
            appendLine("(${lowerUnderscoreFields(javaFields)})")
            appendLine("values")
            appendLine("<foreach collection=\"list\" item=\"item\" separator=\",\">")
            appendLine("(${transferFields(javaFields)})")
            appendLine("</foreach>")
        }.toString()
    }

    private fun getJavaFields(psiClass: PsiClass): List<JavaFieldVO> {
        val psiFields = PsiTreeUtil.findChildrenOfType(psiClass, PsiField::class.java)
        return psiFields.map(PsiJavaConverter::toJavaField)
    }

    private fun lowerUnderscoreFields(javaFields: List<JavaFieldVO>): String {
        return StringBuilder().apply {
            javaFields.forEach {
                append("${CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, it.name)}, ")
            }
        }.toString().removeSuffix(", ")
    }

    private fun transferFields(javaFields: List<JavaFieldVO>): String {
        return StringBuilder().apply {
            javaFields.forEach {
                appendLine("#{item.${it.name},jdbcType=${JdbcTypeConverter.toJdbcType(it.type)}},")
            }
        }.toString().removeSuffix(",\n")
    }
}