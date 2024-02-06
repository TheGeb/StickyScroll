package com.github.kikimanjaro.stickyscroll.marshaller

import com.intellij.psi.*
import com.intellij.psi.util.parents
import org.jetbrains.kotlin.js.translate.utils.finalElement

class DefaultParentMarshaller : PsiParentMarshaller, DefaultTextRangeMarshaller() {

    //TODO: refactor changes into JavaParentMarshaller

    override fun getParents(psiElement: PsiElement?): Sequence<PsiElement>? {
        return kotlin.runCatching {
            psiElement?.parents(false)?.filter {
                    element ->
                           element is PsiClass
                        || element is PsiMethod
                        || element is PsiIfStatement
                        || element is PsiLoopStatement
                        || element is PsiTryStatement
                        || element is PsiSwitchStatement
                        || element is PsiSwitchLabelStatement
            }
        }.getOrNull()
    }
}