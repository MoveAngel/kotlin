/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.idea.inspections

import com.intellij.codeInspection.IntentionWrapper
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import org.jetbrains.kotlin.diagnostics.DiagnosticFactoryWithPsiElement
import org.jetbrains.kotlin.diagnostics.rendering.DefaultErrorMessages
import org.jetbrains.kotlin.idea.caches.resolve.analyzeWithAllCompilerChecks
import org.jetbrains.kotlin.idea.quickfix.QuickFixes
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtVisitorVoid


abstract class AbstractDiagnosticBasedInspection<T : KtElement>(
    private val diagnosticFactory: DiagnosticFactoryWithPsiElement<T, *>,
    private val elementType: Class<T>,
) : AbstractKotlinInspection() {
    final override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor = object : KtVisitorVoid() {
        override fun visitKtElement(element: KtElement) {
            super.visitKtElement(element)

            if (!elementType.isInstance(element) || element.textLength == 0) return
            val diagnostic = element.containingKtFile
                .analyzeWithAllCompilerChecks()
                .bindingContext
                .diagnostics
                .forElement(element)
                .find { it.factory == diagnosticFactory }
                ?: return

            val actionsFactory =
                QuickFixes.getInstance().getActionFactories(diagnosticFactory).singleOrNull() ?: error("Must have one factory")
            val intentionAction = actionsFactory.createActions(diagnostic).singleOrNull() ?: error("Must have one fix")
            val text = DefaultErrorMessages.render(diagnostic)
            holder.registerProblem(element, text, IntentionWrapper(intentionAction, element.containingKtFile))
        }
    }

}