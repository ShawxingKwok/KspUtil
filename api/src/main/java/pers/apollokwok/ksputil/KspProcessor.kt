package pers.apollokwok.ksputil

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated

public interface KspProcessor {
    /**
     * Times start at 1.
     * Error would be caught background and an empty list would be returned next. If so, that error message would be
     * logged out by [KSPLogger.error], allowing other [SymbolProcessor]s completes the current round and stop,
     * which is very helpful for type inferences.
     */
    public fun process(times: Int): List<KSAnnotated>
    public fun onFinish() {}
    public fun onErrorExceptSelfInitialization() {}
}