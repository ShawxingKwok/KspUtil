package pers.apollokwok.ksputil

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.FileLocation
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.NonExistLocation
import com.sun.org.apache.xpath.internal.functions.FuncFalse
import pers.apollokwok.ktutil.updateIf
import kotlin.contracts.contract
import kotlin.reflect.KFunction3

/**
 * This optimized log util allows multiple symbols.
 */
public object Log {
    private val isDebug = "ksp-util.debug" in Environment.options

    @PublishedApi
    internal fun String.addLocations(symbols: List<KSNode>): String =
        symbols.joinToString(
            prefix = this.updateIf({ symbols.any()}){ it + "\n" },
            separator = "\n"
        ) { node ->
            when(val location = node.location) {
                is FileLocation -> "$node at ${location.filePath}:${location.lineNumber}"

                is NonExistLocation ->
                    when(val qualifiedName = (node as? KSDeclaration)?.qualifiedName?.asString()){
                        null -> "$node not in the dest module"
                        else -> "$node at $qualifiedName, not the dest module."
                    }
            }
        }

    // `vararg` would invalidate 'contract'.
    /**
     * This contains [contract] helpful to syntax references like [kotlin.require].
     * If [condition] didn't match, the effect would be like [e].
     */
    public inline fun require(
        condition: Boolean,
        symbols: List<KSNode>,
        lazyMsg: () -> Any?
    ){
        contract {
            returns() implies condition
        }
        require(condition){
            lazyMsg().toString().addLocations(symbols)
        }
    }

    /**
     * This contains [contract] helpful to syntax references like [kotlin.require].
     * If [condition] didn't match, the effect would be like [e].
     */
    public inline fun require(
        condition: Boolean,
        symbol: KSNode,
        lazyMsg: () -> Any?
    ){
        contract {
            returns() implies condition
        }
        require(condition){
            lazyMsg().toString().addLocations(listOf(symbol))
        }
    }

    private fun log(
        undone: Boolean,
        logFun: KFunction3<KSPLogger, String, KSNode?, Unit>,
        msg: Any?,
        symbols: List<KSNode>,
    ){
        if (undone) return
        val locatedMsg = msg.toString().addLocations(symbols)
        logFun.call(Environment.logger, locatedMsg, null)
    }

    /**
     * Log [msg] out with level `logging` and [symbols].
     */
    public fun l(msg: Any?, symbols: List<KSNode>){
        log(!isDebug, KSPLogger::logging, msg, symbols)
    }

    /**
     * Log [msg] out with level `info` and [symbols].
     */
    public fun i(msg: Any?, symbols: List<KSNode>){
        log(!isDebug, KSPLogger::info, msg, symbols)
    }

    /**
     * Log [msg] out with level `warn` and [symbols].
     */
    public fun w(msg: Any?, symbols: List<KSNode>){
        log(false, KSPLogger::warn, msg, symbols)
    }

    /**
     * All [KspProcessor]s would be stopped once the current round completes. And [msg] would be logged out with level
     * `error` and [symbols].
     */
    public fun errorLater(msg: Any?, symbols: List<KSNode>){
        log(false, KSPLogger::error, msg, symbols)
    }

    /**
     * Your [KspProcessor] would be stopped at once, but allowing other [KspProcessor]s completes the current round.
     * And [msg] would be logged out in level `e` with [symbols].
     * Returning [Nothing] is very helpful on type inferences.
     */
    public fun e(msg: Any?, symbols: List<KSNode>): Nothing =
        error(msg.toString().addLocations(symbols))

    /**
     * Log [msg] out with level `logging` and [symbols].
     */
    public fun l(msg: Any?, vararg symbols: KSNode){
        l(msg, symbols.toList())
    }

    /**
     * Log [msg] out with level `info` and [symbols].
     */
    public fun i(msg: Any?, vararg symbols: KSNode){
        i(msg, symbols.toList())
    }

    /**
     * Log [msg] out with level `warn` and [symbols].
     */
    public fun w(msg: Any?, vararg symbols: KSNode){
        w(msg, symbols.toList())
    }

    /**
     * Your [KspProcessor] would be stopped at once, but allowing other [KspProcessor]s completes the current round.
     * And [msg] would be logged out in level `e` with [symbols].
     * Returning [Nothing] is very helpful on type inferences.
     */
    public fun e(msg: Any?, vararg symbols: KSNode): Nothing =
        e(msg, symbols.toList())

    /**
     * [KspProcessor] would be stopped once the current round completes. And [msg] would be logged out in level `e`
     * with [symbols].
     */
    public fun errorLater(msg: Any?, vararg symbols: KSNode){
        errorLater(msg, symbols.toList())
    }
}