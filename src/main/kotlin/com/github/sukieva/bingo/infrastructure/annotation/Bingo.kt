package com.github.sukieva.bingo.infrastructure.annotation

import org.jetbrains.annotations.ApiStatus

/**
 * Annotation for dsl dialog
 *
 * @author SukiEva
 * @since 2023/01/15
 */
@ApiStatus.Internal
@Target(AnnotationTarget.FUNCTION)
internal annotation class Bingo(val title: String, val description: String, val scrollbar: Boolean = false)