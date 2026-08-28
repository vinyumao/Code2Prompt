package com.wayen.code2prompt

import kotlin.test.Test
import kotlin.test.assertEquals

class CodeContextTemplateFormatterTest {

    @Test
    fun `formats a Kotlin selection using the default prompt template`() {
        val context = CodeContext(
            fileName = "HeaderModel.kt",
            absolutePath = "/Users/wayne/work/android-recopos/tool/src/main/java/com/shulin/tool/network/HeaderModel.kt",
            startLine = 129,
            endLine = 129,
            location = "HeaderModel#getLanguage",
            code = "fun getLanguage(key: String):String= languageMmkv.getString(key,LanguageUtil.getLanguage())?:\"\"",
            languageId = "kotlin",
        )

        assertEquals(
            """
            [HeaderModel.kt](/Users/wayne/work/android-recopos/tool/src/main/java/com/shulin/tool/network/HeaderModel.kt)  行号: 129 位置: HeaderModel#getLanguage 关键代码:
            ```kt
            fun getLanguage(key: String):String= languageMmkv.getString(key,LanguageUtil.getLanguage())?:""
            ```

            要做什么:
            """.trimIndent(),
            CodeContextTemplateFormatter.format(context),
        )
    }
}
