package pers.shawxingkwok.ksputil

import com.google.devtools.ksp.symbol.KSDeclaration

// todo: report this hint mistake
@Suppress("RecursivePropertyAccessor")
public val KSDeclaration.outermostDeclaration: KSDeclaration get() =
    parentDeclaration?.outermostDeclaration ?: this