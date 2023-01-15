package com.github.sukieva.bingo.toolbox.converter

import java.sql.JDBCType


/**
 * Java type to JDBC type
 *
 * @author SukiEva
 * @since 2023/01/15
 */
object JdbcTypeConverter {
    private val numericTypes = listOf(
        "int",
        "long",
        "float",
        "double",
        "Integer",
        "Long",
        "Float",
        "Double",
        "BigInteger",
        "BigDecimal"
    )

    private val varcharTypes = listOf("String")

    private val dateTypes = listOf("Date")

    private val boolTypes = listOf("boolean", "Boolean")

    fun toJdbcType(javaType: String): String? {
        return when {
            numericTypes.contains(javaType) -> JDBCType.NUMERIC.name
            varcharTypes.contains(javaType) -> JDBCType.VARCHAR.name
            dateTypes.contains(javaType) -> JDBCType.TIMESTAMP.name
            boolTypes.contains(javaType) -> JDBCType.BOOLEAN.name
            else -> null
        }
    }
}