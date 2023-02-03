package pers.apollokwok.ksputil

import com.google.devtools.ksp.symbol.FileLocation
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.NonExistLocation
import pers.apollokwok.ktutil.updateIf
import kotlin.contracts.contract

/**
 * This optimized log util allows multiple symbols.
 */
public object Log {
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

    /**
     * Log [msg] out with level `d` and [symbols].
     */
    public fun d(msg: Any?, symbols: List<KSNode>){
        Environment.logger.logging(msg.toString().addLocations(symbols))
    }

    /**
     * Log [msg] out with level `i` and [symbols].
     */
    public fun i(msg: Any?, symbols: List<KSNode>){
        Environment.logger.info(msg.toString().addLocations(symbols))
    }

    /**
     * Log [msg] out with level `w` and [symbols].
     */
    public fun w(msg: Any?, symbols: List<KSNode>){
        Environment.logger.warn(msg.toString().addLocations(symbols))
    }

    /**
     * All [KspProcessor]s would be stopped once the current round completes. And [msg] would be logged out in level
     * `e` with [symbols].
     */
    public fun errorLater(msg: Any?, symbols: List<KSNode>){
        Environment.logger.error(msg.toString().addLocations(symbols))
    }

    /**
     * Your [KspProcessor] would be stopped at once, but allowing other [KspProcessor]s completes the current round.
     * And [msg] would be logged out in level `e` with [symbols].
     * Returning [Nothing] is very helpful on type inferences.
     */
    public fun e(msg: Any?, symbols: List<KSNode>): Nothing =
        error(msg.toString().addLocations(symbols))

    /**
     * Log [msg] out with level `d` and [symbols].
     */
    public fun d(msg: Any?, vararg symbols: KSNode){
        d(msg, symbols.toList())
    }

    /**
     * Log [msg] out with level `i` and [symbols].
     */
    public fun i(msg: Any?, vararg symbols: KSNode){
        i(msg, symbols.toList())
    }

    /**
     * Log [msg] out with level `w` and [symbols].
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