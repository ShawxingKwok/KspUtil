package pers.shawxingkwok.ksputil

import com.google.devtools.ksp.symbol.FileLocation
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.NonExistLocation
import java.awt.SystemColor.info
import kotlin.contracts.contract

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

    private val List<KSNode>.locations: String
        get() = joinToString("\n") { it.fixedLocation }

    private val Array<out KSNode>.locations: String
        get() = joinToString("\n") { it.fixedLocation }

    private val isDebug: Boolean = "ksp-util.debug" in Environment.options

    private fun getWholeMessage(obj: Any?, symbols: Array<out KSNode>): String =
        buildString {
            append(obj)
            if (symbols.any()) append("\nat symbols: ${symbols.locations}")
            if (isDebug) append("\nby code: ${Thread.currentThread().stackTrace[3]}")
        }

    private fun getWholeMessage(obj: Any?, symbols: List<KSNode>): String =
        buildString {
            append(obj)
            if (symbols.any()) append("\nat symbols: ${symbols.locations}")
            if (isDebug) append("\nby code: ${Thread.currentThread().stackTrace[3]}")
        }

    /**
     * Log [info] out with level `debug` and [symbols].
     */
    public fun d(
        obj: Any?,
        vararg symbols: KSNode,
    ) {
        //Level `warn is used instead because, the debug message is mixed in massive messy messages at present.
        // todo: change after the official fix.
        if (!isDebug) return
        val message = getWholeMessage(obj, symbols)
        Environment.logger.warn(message)
    }

    /**
     * Log [info] out with level `info` and [symbols].
     */
    public fun i(
        obj: Any?,
        vararg symbols: KSNode,
    ){
        val message = getWholeMessage(obj, symbols)
        Environment.logger.info(message)
    }

    /**
     * Log [info] out with level `info` and [symbols].
     */
    public fun i(
        obj: Any?,
        symbols: List<KSNode>,
    ){
        val message = getWholeMessage(obj, symbols)
        Environment.logger.info(message)
    }

    /**
     * Log [info] out with level `warn` and [symbols].
     */
    public fun w(
        obj: Any?,
        vararg symbols: KSNode,
    ){
        val message = getWholeMessage(obj, symbols)
        Environment.logger.warn(message)
    }

    /**
     * Log [info] out with level `warn` and [symbols].
     */
    public fun w(
        obj: Any?,
        symbols: List<KSNode>,
    ){
        val message = getWholeMessage(obj, symbols)
        Environment.logger.warn(message)
    }

    /**
     * All [KSProcessor]s would be stopped once the current round completes. And [info] would be logged out with level
     * `error` and [symbols].
     */
    public fun e(
        obj: Any?,
        vararg symbols: KSNode,
    ){
        val message = getWholeMessage(obj, symbols)
        Environment.logger.error(message)
    }

    /**
     * All [KSProcessor]s would be stopped once the current round completes. And [info] would be logged out with level
     * `error` and [symbols].
     */
    public fun e(
        obj: Any?,
        symbols: List<KSNode>,
    ){
        val message = getWholeMessage(obj, symbols)
        Environment.logger.error(message)
    }

    /**
     * All [KSProcessor]s would be stopped once the current round completes. And [info] would be logged out with level
     * `error` and [symbols].
     */
    public fun e(
        obj: Any?,
        vararg symbols: KSNode,
        tr: Throwable,
    ){
        var message = getWholeMessage(obj, symbols)

        if (isDebug)
            message += "\n" + tr.stackTraceToString()

        Environment.logger.error(message)
    }

    /**
     * All [KSProcessor]s would be stopped once the current round completes. And [info] would be logged out with level
     * `error` and [symbols].
     */
    public fun e(
        obj: Any?,
        symbols: List<KSNode>,
        tr: Throwable,
    ){
        var message = getWholeMessage(obj, symbols)

        if (isDebug)
            message += "\n" + tr.stackTraceToString()

        Environment.logger.error(message)
    }

    /**
     * Your [KSProcessor] would be stopped at once, but allowing other [KSProcessor]s completes the current round.
     * And [info] would be logged out in level `e` with [symbols].
     *
     * [f] means 'fatal'. Returning [Nothing] is very helpful on type inferences.
     */
    public fun f(
        obj: Any?,
        vararg symbols: KSNode,
    ): Nothing {
        val message = getWholeMessage(obj, symbols)
        error(message)
    }

    /**
     * Your [KSProcessor] would be stopped at once, but allowing other [KSProcessor]s completes the current round.
     * And [info] would be logged out in level `e` with [symbols].
     *
     * [f] means 'fatal'. Returning [Nothing] is very helpful on type inferences.
     */
    public fun f(
        obj: Any?,
        symbols: List<KSNode>,
    ): Nothing {
        val message = getWholeMessage(obj, symbols)
        error(message)
    }

    /**
     * Your [KSProcessor] would be stopped at once, but allowing other [KSProcessor]s completes the current round.
     * And [info] would be logged out in level `e` with [symbols].
     *
     * [f] means 'fatal'. Returning [Nothing] is very helpful on type inferences.
     */
    public fun f(
        obj: Any?,
        vararg symbols: KSNode,
        tr: Throwable,
    ): Nothing {
        val message = getWholeMessage(obj, symbols)
        Environment.logger.error(message)
        throw tr
    }

    /**
     * Your [KSProcessor] would be stopped at once, but allowing other [KSProcessor]s completes the current round.
     * And [info] would be logged out in level `e` with [symbols].
     *
     * [f] means 'fatal'. Returning [Nothing] is very helpful on type inferences.
     */
    public fun f(
        obj: Any?,
        symbols: List<KSNode>,
        tr: Throwable,
    ): Nothing {
        val message = getWholeMessage(obj, symbols)
        Environment.logger.error(message)
        throw tr
    }

    //region require
    // `vararg` would invalidate 'contract'.
    /**
     * This contains [contract] helpful to syntax references like [kotlin.require].
     * If [condition] didn't match, the effect would be like [f].
     */
    public fun require(
        condition: Boolean,
        symbols: List<KSNode>,
        getObj: () -> Any?,
    ){
        contract {
            returns() implies condition
        }
        require(condition){
            getWholeMessage(getObj(), symbols)
        }
    }

    /**
     * This contains [contract] helpful to syntax references like [kotlin.require].
     * If [condition] didn't match, the effect would be like [f].
     */
    public fun require(
        condition: Boolean,
        symbol: KSNode?,
        getObj: () -> Any?,
    ){
        contract {
            returns() implies condition
        }
        require(condition){
            getWholeMessage(getObj(), listOfNotNull(symbol))
        }
    }
    //endregion
}