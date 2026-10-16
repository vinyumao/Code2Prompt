package com.wayen.code2prompt

import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import java.awt.datatransfer.Transferable

class CodeContextTransferable(context: CodeContext) : Transferable {

    private val plainText = StringSelection(CodeContextTemplateFormatter.format(context))
    private val html = CodeContextTemplateFormatter.formatHtml(context)

    override fun getTransferDataFlavors(): Array<DataFlavor> = HTML_FLAVORS + plainText.transferDataFlavors

    override fun isDataFlavorSupported(flavor: DataFlavor): Boolean =
        flavor in HTML_FLAVORS || plainText.isDataFlavorSupported(flavor)

    override fun getTransferData(flavor: DataFlavor): Any =
        if (flavor in HTML_FLAVORS) html else plainText.getTransferData(flavor)

    private companion object {
        val HTML_FLAVORS = arrayOf(
            DataFlavor.selectionHtmlFlavor,
            DataFlavor.fragmentHtmlFlavor,
            DataFlavor.allHtmlFlavor,
        )
    }
}
