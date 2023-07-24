package pers.shawxingkwok.ksputil

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import java.io.File

public lateinit var Environment: SymbolProcessorEnvironment
    private set

public lateinit var resolver: Resolver
    private set

private val allCaches = mutableListOf<MutableMap<*, *>>()

/**
 * For absolute safety.
 *
 * Avoid
 * ```
 * Environment.codeGenerator.previousGeneratedFiles as MutableSet
 */
private var previousGeneratedFiles = setOf<File>()

@Suppress("UnusedReceiverParameter")
public val CodeGenerator.previousGeneratedFiles: Set<File> get() = pers.shawxingkwok.ksputil.previousGeneratedFiles

/**
 * Caches [this], and clear it every round, for saving KSNodes which may be invalid in the next round.
 */
@Synchronized
public fun <T: MutableMap<*, *>> T.alsoRegister(): T = apply { allCaches += this }

/**
 * A simplified and optimized [SymbolProcessorProvider].
 */
public abstract class KSProcessorProvider(private val getProcessor: () -> KSProcessor) : SymbolProcessorProvider {
    final override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor = object : SymbolProcessor {
        init {
            Environment = environment
        }

        var times = 0
        lateinit var processor: KSProcessor

        override fun process(resolver: Resolver): List<KSAnnotated> {
            pers.shawxingkwok.ksputil.resolver = resolver
            return try {
                if (++times == 1)
                    processor = getProcessor()

                processor.process(times)
            } catch (tr: Throwable) {
                Log.e("$tr\n ${tr.stackTrace.joinToString("\n") { "at $it" }}")
                emptyList()
            } finally {
                allCaches.forEach { it.clear() }
                previousGeneratedFiles = previousGeneratedFiles + environment.codeGenerator.generatedFile
            }
        }

        override fun finish() {
            processor.onFinish()
        }

        override fun onError() {
            if (::processor.isInitialized)
                processor.onErrorExceptSelfInitialization()
        }
    }
}