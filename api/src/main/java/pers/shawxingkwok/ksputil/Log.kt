package pers.shawxingkwok.ksputil

import com.google.devtools.ksp.symbol.FileLocation
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.NonExistLocation
import kotlin.contracts.contract

// Default arguments aren't applied because they bother traces.

/**
 * This optimized log util allows multiple symbols.
 */
public object Log{
    private val KSNode.fixedLocation get()  =
        when(val location = this.location) {
            is FileLocation -> "$this at ${location.filePath}:${location.lineNumber}"

            is NonExistLocation ->
                when(val qualifiedName = (this as? KSDeclaration)?.qualifiedName?.asString()){
                    null -> "$this not in the dest module"
                    else -> "$this at $qualifiedName, not the dest module."
                }
        }

    private val isDebug: Boolean = "ksp-util.debug" in Environment.options

    private fun getTraceIfNotDebug(): String? =
        if (isDebug)
            "${Thread.currentThread().stackTrace[3]}"
        else
            null

    private fun getWholeMessage(symbols: List<KSNode>, msg: Any?, trace: String?): String =
        buildString {
            append(msg)
            when(symbols.size){
                0 -> {}
                1 -> append("\nInvolved symbol: ${symbols.first().fixedLocation}")
                else -> {
                    append("\nInvolved symbols:")
                    append(symbols.joinToString(""){ "\n  ${it.fixedLocation}" })
                }
            }
            if (trace != null) append("\nby code: $trace")
        }

    private fun getWholeMessage(symbol: KSNode?, msg: Any?, trace: String?): String =
        getWholeMessage(listOfNotNull(symbol), msg, trace)

    /**
     * Log [msg] out with level `debug` and [symbols] locations.
     */
    public fun d(symbols: List<KSNode>, msg: Any?) {
        //Level `warn is used instead because, the debug message is mixed
        // in massive messy messages at present.
        // todo: change after the official fix.
        if (!isDebug) return
        val message = getWholeMessage(symbols, msg, getTraceIfNotDebug())
        Environment.logger.warn(message)
    }

    /**
     * Log [msg] out with level `debug` and [symbol] location.
     */
    public fun d(symbol: KSNode?, msg: Any?){
        if (!isDebug) return
        val message = getWholeMessage(symbol, msg, getTraceIfNotDebug())
        Environment.logger.warn(message)
    }

    /**
     * Log [msg] out with level `info` and [symbols] locations.
     */
    public fun i(symbols: List<KSNode>, msg: Any?){
        val message = getWholeMessage(symbols, msg, getTraceIfNotDebug())
        Environment.logger.info(message)
    }

    /**
     * Log [msg] out with level `info` and [symbol] location.
     */
    public fun i(symbol: KSNode?, msg: Any?){
        val message = getWholeMessage(symbol, msg, getTraceIfNotDebug())
        Environment.logger.info(message)
    }

    /**
     * Log [msg] out with level `warn` and [symbols] locations.
     */
    public fun w(symbols: List<KSNode>, msg: Any?){
        val message = getWholeMessage(symbols, msg, getTraceIfNotDebug())
        Environment.logger.warn(message)
    }

    /**
     * Log [msg] out with level `warn` and [symbol] locations.
     */
    public fun w(symbol: KSNode?, msg: Any?){
        val message = getWholeMessage(symbol, msg, getTraceIfNotDebug())
        Environment.logger.warn(message)
    }

    /**
     * Your [KSProcessor] would be stopped at once, but allowing other [KSProcessor]s
     * completes the current round.
     * And [msg] would be logged out with level `error` with [symbols] locations.
     *
     * Returning [Nothing] is very helpful on type inferences.
     */
    public fun e(symbols: List<KSNode>, msg: Any?): Nothing {
        val message = getWholeMessage(symbols, msg, null)
        error(message)
    }

    /**
     * Your [KSProcessor] would be stopped at once, but allowing other [KSProcessor]s
     * completes the current round.
     * And [msg] would be logged out with level `error` with [symbol] location.
     *
     * Returning [Nothing] is very helpful on type inferences.
     */
    public fun e(symbol: KSNode?, msg: Any?): Nothing {
        val message = getWholeMessage(symbol, msg, null)
        error(message)
    }

    /**
     * Your [KSProcessor] would be stopped at once, but allowing other [KSProcessor]s
     * completes the current round.
     * And [msg] would be logged out with level `error`, [tr] traces and [symbols] locations.
     *
     * Returning [Nothing] is very helpful on type inferences.
     */
    public fun e(symbols: List<KSNode>, msg: Any?, tr: Throwable): Nothing {
        val message = getWholeMessage(symbols, msg, null)
        error("$message\n${tr.stackTraceToString()}")
    }

    /**
     * Your [KSProcessor] would be stopped at once, but allowing other [KSProcessor]s
     * completes the current round.
     * And [msg] would be logged out with level `error`, [tr] traces and [symbol] location.
     *
     * Returning [Nothing] is very helpful on type inferences.
     */
    public fun e(symbol: KSNode?, msg: Any?, tr: Throwable): Nothing {
        val message = getWholeMessage(symbol, msg, null)
        error("$message\n${tr.stackTraceToString()}")
    }

    //region check
    // `vararg` would invalidate 'contract' at present.
    /**
     * This contains [contract] helpful to syntax references like [kotlin.require].
     * If [condition] didn't match, the effect would be like [f].
     */
    public fun check(
        symbols: List<KSNode>,
        condition: Boolean,
        getMsg: () -> Any?,
    ){
        contract {
            returns() implies condition
        }
        check(condition){
            getWholeMessage(symbols, getMsg(), null)
        }
    }

    /**
     * This contains [contract] helpful to syntax references like [kotlin.require].
     * If [condition] didn't match, the effect would be like [f].
     */
    public fun check(
        symbol: KSNode?,
        condition: Boolean,
        getMsg: () -> Any?,
    ){
        contract {
            returns() implies condition
        }
        check(condition){
            getWholeMessage(listOfNotNull(symbol), getMsg(), null)
        }
    }
    //endregion
}