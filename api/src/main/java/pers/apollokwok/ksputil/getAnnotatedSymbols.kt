package pers.apollokwok.ksputil

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import java.util.concurrent.ConcurrentHashMap

@PublishedApi
internal val annotatedSymbolsCache: Any = ConcurrentHashMap<Any, List<KSAnnotated>>().alsoRegister()

/**
 * Returns [A]s annotated with [T]. These symbols are cached and cleared every round.
 */
@Suppress("UNCHECKED_CAST")
public inline fun <reified T: Annotation, reified A: KSAnnotated> Resolver.getAnnotatedSymbols(): List<A> =
    (annotatedSymbolsCache as MutableMap<Any, List<KSAnnotated>>)
        .getOrPut(T::class to A::class) {
            getSymbolsWithAnnotation(T::class.qualifiedName!!)
            .filterIsInstance<A>()
            // todo: remove this line after the authoritative fix.
            .distinct()
            .toList()
        } as List<A>