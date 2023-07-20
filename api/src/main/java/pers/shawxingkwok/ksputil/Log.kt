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
    @PublishedApi
    internal fun Array<out KSNode>.locations(): String =
        joinToString(separator = "\n") { node ->
            when(val location = node.location) {
                is FileLocation -> "$node at ${location.filePath}:${location.lineNumber}"

                is NonExistLocation ->
                    when(val qualifiedName = (node as? KSDeclaration)?.qualifiedName?.asString()){
                        null -> "$node not in the dest module"
                        else -> "$node at $qualifiedName, not the dest module."
                    }
            }
        }

    @PublishedApi
    internal val isDebug: Boolean = "ksp-util.debug" in Environment.options

    private fun getMsgWithLocationsOrSingleTraceFurther(coreMsg: Any?, symbols: Array<out KSNode>): String =
        buildString {
            append(coreMsg)
            if (symbols.any()) append("\nat symbols: ${symbols.locations()}")
            if (isDebug) append("\nby code: ${Thread.currentThread().stackTrace[3]}")
        }

    /**
     * Log [info] out with level `debug` and [symbols].
     */
    public fun d(
        coreMsg: Any?,
        vararg symbols: KSNode,
    ) {
        // At present, the debug message is mixed in massive messy messages. So, I use this instead temporarily.
        // todo: change after the official fix.
        if (!isDebug) return
        val msg = getMsgWithLocationsOrSingleTraceFurther(coreMsg, symbols)
        Environment.logger.warn(msg)
    }

    /**
     * Log [info] out with level `info` and [symbols].
     */
    public fun i(
        coreMsg: Any?,
        vararg symbols: KSNode,
    ){
        val msg = getMsgWithLocationsOrSingleTraceFurther(coreMsg, symbols)
        Environment.logger.info(msg)
    }

    /**
     * Log [info] out with level `warn` and [symbols].
     */
    public fun w(
        coreMsg: Any?,
        vararg symbols: KSNode,
    ){
        val msg = getMsgWithLocationsOrSingleTraceFurther(coreMsg, symbols)
        Environment.logger.warn(msg)
    }

    /**
     * All [KSProcessor]s would be stopped once the current round completes. And [info] would be logged out with level
     * `error` and [symbols].
     */
    public fun e(
        coreMsg: Any?,
        vararg symbols: KSNode,
    ){
        val msg = getMsgWithLocationsOrSingleTraceFurther(coreMsg, symbols)
        Environment.logger.error(msg)
    }

    /**
     * All [KSProcessor]s would be stopped once the current round completes. And [info] would be logged out with level
     * `error` and [symbols].
     */
    public fun e(
        coreMsg: Any?,
        vararg symbols: KSNode,
        tr: Throwable,
    ){
        var msg = getMsgWithLocationsOrSingleTraceFurther(coreMsg, symbols)

        if (isDebug)
            msg += "\n" + tr.stackTraceToString()

        Environment.logger.error(msg)
    }

    /**
     * Your [KSProcessor] would be stopped at once, but allowing other [KSProcessor]s completes the current round.
     * And [info] would be logged out in level `e` with [symbols].
     *
     * [f] means 'fatal'. Returning [Nothing] is very helpful on type inferences.
     */
    public fun f(
        coreMsg: Any?,
        vararg symbols: KSNode,
    ): Nothing {
        val msg = getMsgWithLocationsOrSingleTraceFurther(coreMsg, symbols)
        error(msg)
    }

    public fun f(
        coreMsg: Any?,
        vararg symbols: KSNode,
        tr: Throwable,
    ): Nothing {
        val msg = getMsgWithLocationsOrSingleTraceFurther(coreMsg, symbols)
        Environment.logger.error(msg)
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
        lazyMsg: () -> Any?,
    ){
        contract {
            returns() implies condition
        }
        require(condition){
            getMsgWithLocationsOrSingleTraceFurther(lazyMsg(), symbols.toTypedArray())
        }
    }

    public fun require(
        condition: Boolean,
        symbol: KSNode?,
        lazyMsg: () -> Any?,
    ){
        contract {
            returns() implies condition
        }
        require(condition){
            getMsgWithLocationsOrSingleTraceFurther(lazyMsg(), listOfNotNull(symbol).toTypedArray())
        }
    }
    //endregion
}