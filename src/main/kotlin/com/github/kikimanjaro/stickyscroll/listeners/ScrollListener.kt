package com.github.kikimanjaro.stickyscroll.listeners

import com.github.kikimanjaro.stickyscroll.config.StickyScrollConfigService.Companion.ConfigInstance
import com.github.kikimanjaro.stickyscroll.marshaller.PsiParentMarshallerManager
import com.github.kikimanjaro.stickyscroll.services.StickyPanelManager
import com.github.kikimanjaro.stickyscroll.ui.MyEditorFragmentComponent
import com.intellij.openapi.Disposable
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.editor.event.VisibleAreaEvent
import com.intellij.openapi.editor.event.VisibleAreaListener
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.util.ui.JBUI
import org.jetbrains.kotlin.psi.psiUtil.endOffset
import org.jetbrains.kotlin.psi.psiUtil.startOffset
import java.awt.Point

class ScrollListener(val stickyPanelManager: StickyPanelManager) : VisibleAreaListener, Disposable {

    val editor = stickyPanelManager.editor
    var lastYDelta = 0

    init {
        editor.scrollingModel.addVisibleAreaListener(this, stickyPanelManager)
    }

    override fun visibleAreaChanged(e: VisibleAreaEvent) {
        val logicalPosition = editor.xyToLogicalPosition(
            Point(
                editor.scrollingModel.visibleArea.width, editor.scrollingModel.visibleArea.y
            )
        )
        val positionToOffset = editor.logicalPositionToOffset(logicalPosition)


        val document = editor.document
        stickyPanelManager.clearPanelList()
        if (document.getLineNumber(positionToOffset) > 0) {
            val psiFile: PsiFile? = PsiDocumentManager.getInstance(stickyPanelManager.project).getPsiFile(document)
            val currentElement = psiFile?.findElementAt(positionToOffset)

            val parentMarshaller = PsiParentMarshallerManager.getParentMarshaller(psiFile?.language)

            val parents = parentMarshaller?.getParents(currentElement)
            var yDelta = 0

            //TODO: scroll fixing
            editor.scrollPane
            editor.scrollingModel

            if (parents != null) {
                for (parent in parents.toList().reversed().take(ConfigInstance.state.maxLine)) {
                    val result = parentMarshaller.getTextRangeAndStartLine(parent, document)
                    if (currentElement != null && document.getLineNumber(currentElement.startOffset) + yDelta + 1 > result.second) {
                        yDelta += 1
                        val hint = MyEditorFragmentComponent.showEditorFragmentHint(
                            editor, result.first, true, false, 1 * editor.lineHeight, result.second
                        )
                        hint?.let { stickyPanelManager.addPanel(it, result.second) }
                    }
                }

//                if (lastYDelta != yDelta) {
//                    editor.xyToLogicalPosition(
//                        Point(
//                            editor.scrollingModel.visibleArea.width, editor.scrollingModel.visibleArea.y
//                        )
//
//                    editor.scrollingModel.scrollTo(logicalPosition, ScrollType.RELATIVE)
//                    lastYDelta = yDelta
//                }
            }

            stickyPanelManager.addTopLabels()
        }

    }

    override fun dispose() {
        editor.scrollingModel.removeVisibleAreaListener(this)
    }
}