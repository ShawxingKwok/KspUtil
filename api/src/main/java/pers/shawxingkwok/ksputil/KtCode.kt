package pers.shawxingkwok.ksputil

import com.google.devtools.ksp.isLocal
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Variance
import kotlin.reflect.KClass

public class KtGen internal constructor(fixedImports: List<String>){
    private val starPackageNames = mutableSetOf<String>()
    private val commonImports = mutableMapOf<String, String>()

    init {
        addImports(fixedImports)
    }

    /**
     * When you need a nested class, you should import its outermost class.
     */
    internal fun addImport(import: String): Boolean =
        if (import.endsWith(".*"))
            starPackageNames.add(import.substringBeforeLast(".*"))
        else {
            val packageName = import.substringBeforeLast(".")

            packageName in starPackageNames || run {
                val name = import.substringAfterLast(".")
                val v = commonImports.putIfAbsent(name, import)
                v == null || v == import
            }
        }

    internal fun addImports(imports: List<String>){
        imports.forEach(::addImport)
    }

    internal fun getImportBody(): String =
        starPackageNames.map { it.substringBeforeLast(".*") }
            .plus(commonImports.values)
            .joinToString("\n"){ "import $it" }
}

context (KtGen)
public val KSType.text: String get() = buildString {
    append(declaration.text)

    if (arguments.any())
        arguments.joinToString(prefix = "<", postfix = ">", separator = ", "){
            when(it.variance){
                Variance.STAR -> "*"
                Variance.INVARIANT -> it.type!!.resolve().text
                else -> it.variance.label + " " + it.type!!.resolve().text
            }
        }
        .let(::append)

    if (this@text.isMarkedNullable)
        append("?")
}

context (KtGen)
public val KClass<*>.text: String get() =
    if (this.java.isLocalClass)
        simpleName!!
    else{
        var clazz = this.java

        while (true)
            clazz = clazz.enclosingClass ?: break

        if (addImport(clazz.canonicalName)) {
            val packageName = java.`package`?.name
            if (packageName == null)
                qualifiedName!!
            else
                qualifiedName!!.substringAfter("$packageName.")
        } else
            this.qualifiedName!!
    }

context (KtGen)
public val KSDeclaration.text: String get() =
    if (isLocal())
        simpleName()
    else{
        val outermostPath = outermostDeclaration.qualifiedName()!!

        if (addImport(outermostPath))
            noPackageName()!!
        else
            qualifiedName()!!
    }