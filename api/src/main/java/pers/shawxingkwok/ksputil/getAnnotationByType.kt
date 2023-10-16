package pers.shawxingkwok.ksputil

import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.symbol.KSAnnotated
import kotlin.reflect.KClass

/**
 * Returns the first annotation of [T] on [this], or `null` if it's not found.
 */
public fun <T : Annotation> KSAnnotated.getAnnotationByType(annotationKClass: KClass<T>): T? =
    getAnnotationsByType(annotationKClass).firstOrNull()