package com.github.kikimanjaro.stickyscroll.marshaller

import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.refactoring.suggested.endOffset
import com.intellij.refactoring.suggested.startOffset
import org.jetbrains.uast.util.ClassSet
import org.jetbrains.uast.util.classSetOf
import java.util.function.Predicate

abstract class DefaultTextRangeMarshaller : PsiParentMarshaller {

    //TODO: refactor changes into JavaParentMarshaller

    private val topLevelTypes : ClassSet<PsiElement> = classSetOf(
        PsiIdentifier::class.java,
        PsiKeyword::class.java
    )

// try/catch/finally is tricky.
// catch() { is represented by PsiCatchSection
// finally { is represented by PsiKeyword with myType = FINALLY_KEYWORD and myText = finally

// if is easy, but else if/else is tricky
// else is represented by PsiKeyword with myType = ELSE_KEYWORD and myText = else
// else if is represented by PsiKeywordElse followed by PsiWhiteSpace and a separate PsiIfStatement?

    private val ignoredTypes : ClassSet<PsiElement> = classSetOf(
        PsiComment::class.java,
        PsiWhiteSpace::class.java,
        PsiModifierList::class.java,
        PsiTryStatement::class.java,
        PsiCodeBlock::class.java)

    override fun getTextRangeAndStartLine(element: PsiElement, document: Document): Pair<TextRange, Int> {
        val startElement: PsiElement = getStartElement(element);

        val startOffset = getStartOffset(startElement)
        val endOffset = getEndOffset(startElement)
        val startLine = document.getLineNumber(startOffset)
        val endLine = document.getLineNumber(startOffset)
        return Pair(TextRange(startOffset, endOffset), document.getLineNumber(startOffset))
//        val realText = document.getText(textRange)
    }

    private fun getStartOffset(element:PsiElement) : Int {
        for(child in element.children) {
            if(!ignoredTypes.contains(child::class.java)) {
                return child.startOffset
            }
        }
        return element.startOffset
    }

    private fun getEndOffset(element:PsiElement) : Int {
        for(child in element.children.reversed()) {
            if(!ignoredTypes.contains(child::class.java)) {
                return child.endOffset
            }
        }
        return element.endOffset
    }

    private fun getStartElement(element:PsiElement) : PsiElement {
        return if (topLevelTypes.contains(element::class.java)) {
            element
        } else {
            findMatchingChildRecursively(element.children) ?: element
        }
    }

    private fun findMatchingChildRecursively(elements: Array<out PsiElement>) : PsiElement? {
        for (element in elements) {

            val checkElement = if (ignoredTypes.contains(element::class.java)) {
                null
            } else if (topLevelTypes.contains(element::class.java)) {
                element
            } else {
                findMatchingChildRecursively(element.children)
            }

            if (checkElement != null) {
                return checkElement
            }
        }
        return null;
    }
}