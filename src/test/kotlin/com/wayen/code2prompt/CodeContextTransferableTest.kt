package com.wayen.code2prompt

import org.xml.sax.InputSource
import java.awt.datatransfer.DataFlavor
import java.io.StringReader
import java.net.URI
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CodeContextTransferableTest {

    @Test
    fun `clipboard supplies rich HTML alongside the original Markdown`() {
        val context = context()
        val clipboard = CodeContextTransferable(context)

        assertEquals(CodeContextTemplateFormatter.format(context), clipboard.getTransferData(DataFlavor.stringFlavor))
        for (flavor in listOf(DataFlavor.selectionHtmlFlavor, DataFlavor.fragmentHtmlFlavor, DataFlavor.allHtmlFlavor)) {
            assertTrue(clipboard.isDataFlavorSupported(flavor))
            assertTrue(flavor in clipboard.transferDataFlavors)
            val document = parseHtml(clipboard.getTransferData(flavor) as String)
            val code = document.getElementsByTagName("code").item(0) as org.w3c.dom.Element
            assertEquals("pre", code.parentNode.nodeName)
            assertEquals("language-kotlin", code.getAttribute("class"))
            assertEquals(context.code, code.textContent)
            assertTrue(document.documentElement.textContent.contains("行号: 58-60 位置: DiagnosticArchive 关键代码:"))
            val trailingParagraphs = generateSequence(code.parentNode.nextSibling) { it.nextSibling }.toList()
            assertEquals(listOf("p", "p"), trailingParagraphs.map { it.nodeName })
            assertTrue(trailingParagraphs.all { it.textContent.isEmpty() })
        }
    }

    @Test
    fun `local file remains a Codex file reference even with special characters in its path`() {
        val context = context().copy(
            fileName = "诊断 & \"archive\".kt",
            absolutePath = "/Users/wayne/测试项目/诊断 & \"archive\".kt",
        )
        val document = parseHtml(CodeContextTransferable(context).getTransferData(DataFlavor.selectionHtmlFlavor) as String)
        val link = document.getElementsByTagName("a").item(0) as org.w3c.dom.Element
        val mention = link.getElementsByTagName("span").item(0) as org.w3c.dom.Element

        assertEquals(context.absolutePath, URI(link.getAttribute("href")).path)
        assertEquals(context.fileName, link.textContent)
        assertEquals(context.fileName, mention.getAttribute("at-mention-label"))
        assertEquals(context.absolutePath, mention.getAttribute("at-mention-path"))
        assertEquals(context.absolutePath, mention.getAttribute("at-mention-fs-path"))
    }

    @Test
    fun `HTML preserves code whitespace and escapes markup without creating elements`() {
        val context = context().copy(
            location = "DiagnosticArchive#load<&>\"'",
            code = "\tval html = \"<script>alert('x')</script> & <tag>\"\n\n    val 演示 = 1  \n",
            languageId = "JAVA",
        )
        val document = parseHtml(CodeContextTransferable(context).getTransferData(DataFlavor.fragmentHtmlFlavor) as String)
        val code = document.getElementsByTagName("code").item(0) as org.w3c.dom.Element

        assertEquals(context.code, code.textContent)
        assertEquals("language-java", code.getAttribute("class"))
        assertEquals(0, document.getElementsByTagName("script").length)
        assertEquals(0, document.getElementsByTagName("tag").length)
        assertTrue(document.documentElement.textContent.contains(context.location!!))
    }

    private fun parseHtml(html: String) = DocumentBuilderFactory.newInstance()
        .newDocumentBuilder()
        .parse(InputSource(StringReader(html)))

    private fun context() = CodeContext(
        fileName = "DiagnosticArchive.kt",
        absolutePath = "/Users/wayne/work/android-recopos/DiagnosticArchive.kt",
        startLine = 58,
        endLine = 60,
        location = "DiagnosticArchive",
        code = "    private fun requireRegularFile(root: File, file: File) {\n" +
            "        require(file.isFile && file.canonicalFile == File(root, file.name)) { \"Invalid diagnostic source\" }\n" +
            "    }",
        languageId = "kotlin",
    )
}
