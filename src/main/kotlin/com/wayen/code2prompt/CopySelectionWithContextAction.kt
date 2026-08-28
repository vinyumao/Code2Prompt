package com.wayen.code2prompt

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.PsiManager
import java.awt.datatransfer.StringSelection

class CopySelectionWithContextAction : AnAction() {

    override fun update(event: AnActionEvent) {
        val editor = event.getData(CommonDataKeys.EDITOR)
        event.presentation.isEnabledAndVisible =
            editor?.selectionModel?.hasSelection() == true &&
                editor.document.let { FileDocumentManager.getInstance().getFile(it) != null }
    }

    override fun actionPerformed(event: AnActionEvent) {
        val editor = event.getData(CommonDataKeys.EDITOR) ?: return
        val project = event.project ?: return
        val virtualFile = FileDocumentManager.getInstance().getFile(editor.document) ?: return
        val psiFile = PsiManager.getInstance(project).findFile(virtualFile) ?: return
        val selection = editor.selectionModel
        val selectedCode = selection.selectedText ?: return

        val startLine = editor.document.getLineNumber(selection.selectionStart) + 1
        val finalSelectedOffset = (selection.selectionEnd - 1).coerceAtLeast(selection.selectionStart)
        val endLine = editor.document.getLineNumber(finalSelectedOffset) + 1
        val context = CodeContext(
            fileName = virtualFile.name,
            absolutePath = virtualFile.path,
            startLine = startLine,
            endLine = endLine,
            location = findLocation(psiFile.findElementAt(selection.selectionStart)),
            code = selectedCode,
            languageId = psiFile.language.id,
        )

        CopyPasteManager.getInstance().setContents(StringSelection(CodeContextTemplateFormatter.format(context)))
    }

    private fun findLocation(start: PsiElement?): String? {
        var element = start
        var memberName: String? = null
        var containerName: String? = null

        while (element != null) {
            val namedElement = element as? PsiNamedElement
            val name = namedElement?.name
            when {
                memberName == null && element.isFunctionLike() -> memberName = name
                containerName == null && element.isClassLike() -> containerName = name
            }
            element = element.parent
        }

        return when {
            containerName != null && memberName != null -> "$containerName#$memberName"
            memberName != null -> memberName
            containerName != null -> containerName
            else -> null
        }
    }

    private fun PsiElement.isFunctionLike(): Boolean =
        this is PsiMethod || javaClass.name == "org.jetbrains.kotlin.psi.KtNamedFunction"

    private fun PsiElement.isClassLike(): Boolean =
        this is PsiClass || javaClass.name in setOf(
            "org.jetbrains.kotlin.psi.KtClass",
            "org.jetbrains.kotlin.psi.KtObjectDeclaration",
        )
}
