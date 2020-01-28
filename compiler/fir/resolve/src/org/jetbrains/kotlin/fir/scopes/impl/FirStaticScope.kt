/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.scopes.impl

import org.jetbrains.kotlin.fir.declarations.FirCallableMemberDeclaration
import org.jetbrains.kotlin.fir.declarations.FirSimpleFunction
import org.jetbrains.kotlin.fir.declarations.isStatic
import org.jetbrains.kotlin.fir.scopes.FirScope
import org.jetbrains.kotlin.fir.symbols.impl.FirCallableSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassifierSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirFunctionSymbol
import org.jetbrains.kotlin.name.Name

class FirStaticScope(
    private val delegateScope: FirScope,
    private val callableFilter: (FirCallableSymbol<*>) -> Boolean = { true }
) : FirScope() {

    override fun processClassifiersByName(name: Name, processor: (FirClassifierSymbol<*>) -> Unit) {
        delegateScope.processClassifiersByName(name) {
            processor(it)
        }
    }

    override fun processFunctionsByName(
        name: Name,
        processor: (FirFunctionSymbol<*>) -> Unit
    ) {
        delegateScope.processFunctionsByName(name) {
            if ((it.fir as? FirSimpleFunction)?.isStatic == true && callableFilter(it)) {
                processor(it)
            }
        }
    }

    override fun processPropertiesByName(
        name: Name,
        processor: (FirCallableSymbol<*>) -> Unit
    ) {
        delegateScope.processPropertiesByName(name) {
            if ((it.fir as? FirCallableMemberDeclaration<*>)?.isStatic == true && callableFilter(it)) {
                processor(it)
            }
        }
    }
}