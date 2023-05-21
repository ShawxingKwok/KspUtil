package pers.apollokwok.ksputil

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated

/**
 * Catches error except self initialization background. If so, an empty list would be returned next, and that
 * error message would be logged out by [KSPLogger.error], allowing other [SymbolProcessor]s completes the
 * current round and stop, which is very helpful for type inferences.
 *
 * Allows you to initialize instance values via [Environment] and [resolver] before [process], since they are
 * global and initialized before [KSProcessor]. Besides, try catch and handle if some instance value
 * initializations are risky.
 */
public interface KSProcessor {
    /**
     * Times start at 1.
     */
    public fun process(times: Int): List<KSAnnotated>
    public fun onFinish() {}
    public fun onErrorExceptSelfInitialization() {}
}