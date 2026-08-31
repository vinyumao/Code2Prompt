package com.wayen.code2prompt

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.util.IconLoader
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.PsiManager
import com.intellij.util.Alarm
import java.awt.datatransfer.StringSelection

class CopySelectionWithContextAction : AnAction() {

    private val feedbackAlarm = Alarm(Alarm.ThreadToUse.SWING_THREAD, ApplicationManager.getApplication())

    @Volatile
    private var successFeedbackUntilMillis = 0L

    @Volatile
    private var feedbackPresentation: Presentation? = null

    override fun update(event: AnActionEvent) {
        val editor = event.getData(CommonDataKeys.EDITOR)
        event.presentation.isEnabled =
            editor?.selectionModel?.hasSelection() == true &&
                editor.document.let { FileDocumentManager.getInstance().getFile(it) != null }

        if (System.currentTimeMillis() < successFeedbackUntilMillis) {
            event.presentation.showCopySuccess()
        } else {
            event.presentation.restoreCopyPresentation()
        }
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

        try {
            CopyPasteManager.getInstance().setContents(StringSelection(CodeContextTemplateFormatter.format(context)))
            showCopySuccess(event.presentation, context)
            notifyCopySuccess(project, context)
        } catch (exception: Exception) {
            notifyCopyFailure(project, exception)
        }
    }

    private fun showCopySuccess(presentation: Presentation, context: CodeContext) {
        successFeedbackUntilMillis = System.currentTimeMillis() + COPY_SUCCESS_FEEDBACK_MILLIS
        feedbackPresentation = presentation
        presentation.showCopySuccess()
        feedbackAlarm.cancelAllRequests()
        feedbackAlarm.addRequest(
            {
                successFeedbackUntilMillis = 0
                feedbackPresentation?.restoreCopyPresentation()
            },
            COPY_SUCCESS_FEEDBACK_MILLIS,
        )
    }

    private fun notifyCopySuccess(project: com.intellij.openapi.project.Project, context: CodeContext) {
        val location = context.location?.let { "（$it）" }.orEmpty()
        NotificationGroupManager.getInstance()
            .getNotificationGroup(NOTIFICATION_GROUP_ID)
            .createNotification(
                "已复制 ${context.fileName} 第 ${context.startLine}–${context.endLine} 行$location",
                NotificationType.INFORMATION,
            )
            .notify(project)
    }

    private fun notifyCopyFailure(project: com.intellij.openapi.project.Project, exception: Exception) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup(NOTIFICATION_GROUP_ID)
            .createNotification(
                "复制失败：${exception.message ?: "无法写入剪贴板"}",
                NotificationType.ERROR,
            )
            .notify(project)
    }

    private fun Presentation.showCopySuccess() {
        icon = COPY_SUCCESS_ICON
        description = "已复制，稍后恢复复制图标"
    }

    private fun Presentation.restoreCopyPresentation() {
        icon = COPY_ICON
        description = COPY_DESCRIPTION
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

    private companion object {
        const val NOTIFICATION_GROUP_ID = "Code2Prompt"
        const val COPY_SUCCESS_FEEDBACK_MILLIS = 1_500L
        const val COPY_DESCRIPTION = "Copy selected code with file, line, and symbol context"

        val COPY_ICON = AllIcons.Actions.Copy
        val COPY_SUCCESS_ICON = IconLoader.getIcon("/icons/copy-success.svg", CopySelectionWithContextAction::class.java)
    }
}
