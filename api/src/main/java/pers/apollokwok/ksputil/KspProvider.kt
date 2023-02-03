package pers.apollokwok.ksputil

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import java.io.File
import java.util.*

public lateinit var Environment: SymbolProcessorEnvironment
    private set

public lateinit var resolver: Resolver

private val allCaches = mutableListOf<MutableMap<*, *>>()

public val CodeGenerator.previousGeneratedFiles: MutableList<File> by lazy { mutableListOf() }

/**
 * Caches [this], and clear it every round, for saving KSNodes which may be invalid in the next round.
 */
@Synchronized
public fun <T: MutableMap<*, *>> T.alsoRegister(): T = apply { allCaches += this }

// todo: introduce this into the authoritative ksp library.
/**
 * A simplified and optimized [SymbolProcessorProvider].
 */
public abstract class KspProvider(
    private val getTestProcessor: (()-> KspProcessor)?,
    private val getProcessor: ()-> KspProcessor,
) : SymbolProcessorProvider {
    // todo: switch the implementation detailTestTag to a gradle argument
    // and here returns that SymbolProcessor directly.
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor = object : SymbolProcessor {
        init {
            Environment = environment
        }

        var times = 0
        lateinit var processor: KspProcessor

        override fun process(resolver: Resolver): List<KSAnnotated> {
            pers.apollokwok.ksputil.resolver = resolver
            return try {
                @Suppress("UNNECESSARY_NOT_NULL_ASSERTION")
                if (++times == 1){
                    processor =
                        if ("ksputil.unitTest" in environment.options){
                            require(getTestProcessor != null){
                                ::getTestProcessor.name + "can't be null when building unittest module."
                            }
                            getTestProcessor!!()
                        }else
                            getProcessor()
                }
                processor.process(times)
            } catch (e: Exception) {
                environment.logger.error(
                    message = "$e\n ${e.stackTrace.joinToString("\n") { "at $it" }}",
                    symbol = null
                )
                emptyList()
            } finally {
                allCaches.forEach { it.clear() }
                environment.codeGenerator.previousGeneratedFiles += environment.codeGenerator.generatedFile
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