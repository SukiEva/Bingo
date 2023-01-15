package com.github.sukieva.bingo.toolbox.vo

/**
 * JavaVO
 *
 * @author SukiEva
 * @since 2023/01/15
 */
data class JavaVO(
    var fields: List<JavaFieldVO> = mutableListOf()
)

data class JavaFieldVO(
    var modifier: String = "",
    var type: String = "",
    var name: String = ""
)