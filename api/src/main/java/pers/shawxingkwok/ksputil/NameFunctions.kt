package pers.shawxingkwok.ksputil

import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFile

public fun KSDeclaration.simpleName(): String = simpleName.asString()
public fun KSDeclaration.packageName(): String = packageName.asString()
public fun KSFile.packageName(): String = packageName.asString()

public fun KSDeclaration.qualifiedName(): String? = qualifiedName?.asString()

/**
 * Warning: returns null if [this] is local.
 */
public fun KSDeclaration.noPackageName(): String?{
    val qualifiedName = qualifiedName() ?: return null
    if (packageName().none())
        return qualifiedName
    else
        return qualifiedName.substringAfter(packageName() + ".")
}