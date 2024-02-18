package com.github.sukieva.bingo.util.ui

import com.google.gson.GsonBuilder
import com.intellij.openapi.ide.CopyPasteManager
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection

/**
 * 剪贴板工具封装
 *
 * @author Suki
 * @since 2024-02-18
 */
object ClipboardUtils {
    fun getText(): String? = CopyPasteManager.getInstance().getContents(DataFlavor.stringFlavor);

    fun <T> setJson(text: T) {
        val jsonStr = GsonBuilder().setPrettyPrinting().create().toJson(text)
        setText(jsonStr)
    }

    fun setText(text: String) {
        val stringSelection = StringSelection(text)
        CopyPasteManager.getInstance().setContents(stringSelection)
    }
}