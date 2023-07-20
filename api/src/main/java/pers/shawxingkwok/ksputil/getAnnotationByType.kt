package pers.shawxingkwok.ksputil

import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.symbol.KSAnnotated

/**
 * Returns the first annotation of [T] on [this], or `null` if it's not found.
 */
public inline fun <reified T : Annotation> KSAnnotated.getAnnotationByType(): T? =
    getAnnotationsByType(T::class).firstOrNull()