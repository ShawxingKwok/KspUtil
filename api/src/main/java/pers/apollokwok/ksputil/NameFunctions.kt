package pers.apollokwok.ksputil

import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFile
import kotlin.reflect.KClass

public fun KSDeclaration.simpleName(): String = simpleName.asString()
public fun KSDeclaration.packageName(): String = packageName.asString()
public fun KSFile.packageName(): String = packageName.asString()

public fun KSDeclaration.qualifiedName(): String? = qualifiedName?.asString()

public fun KSDeclaration.noPackageName(): String? =
    qualifiedName()?.substringAfter(packageName.asString() + ".")

public fun KClass<*>.noPackageName(): String? =
    qualifiedName?.substringAfter(java.`package`.name + ".")