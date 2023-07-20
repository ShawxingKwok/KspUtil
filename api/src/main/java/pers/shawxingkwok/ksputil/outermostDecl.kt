package pers.shawxingkwok.ksputil

import com.google.devtools.ksp.symbol.KSDeclaration

// todo: report this hint mistake
@Suppress("RecursivePropertyAccessor")
public val KSDeclaration.outermostDecl: KSDeclaration get() =
    parentDeclaration?.outermostDecl ?: this