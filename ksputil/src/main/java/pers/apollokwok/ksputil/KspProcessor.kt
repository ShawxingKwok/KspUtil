package pers.apollokwok.ksputil

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.getFunctionDeclarationsByName
import com.google.devtools.ksp.getPropertyDeclarationByName
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty

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

    public abstract class Test{
        protected fun KProperty<*>.toKs(): KSPropertyDeclaration =
            resolver.getPropertyDeclarationByName("${this@Test.javaClass.canonicalName}.$name")!!

        protected fun KFunction<*>.toKs(): KSFunctionDeclaration =
            resolver.getFunctionDeclarationsByName("${this@Test.javaClass.canonicalName}.$name").first()

        protected fun KClass<*>.toKs(): KSClassDeclaration = resolver.getClassDeclarationByName(qualifiedName!!)!!
    }
}