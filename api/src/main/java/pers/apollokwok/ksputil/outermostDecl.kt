package pers.apollokwok.ksputil

import com.google.devtools.ksp.symbol.KSDeclaration

public val KSDeclaration.outermostDecl: KSDeclaration get() =
    parentDeclaration?.outermostDecl ?: this