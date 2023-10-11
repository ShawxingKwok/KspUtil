package pers.shawxingkwok.ksputil

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.isLocal
import com.google.devtools.ksp.isPrivate
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.Variance
import pers.shawxingkwok.ktutil.fastLazy
import kotlin.reflect.KClass

public class KtGen internal constructor(
    private val packageName: String,
    initialImports: List<String>,
) {
    private companion object {
        val autoImportedDeclNames = AutoImportedPackageNames
                .flatMap { resolver.getDeclarationsFromPackage(it) }
                .filter { it.isPublic() }
                .map { it.simpleName() }
                .toSet()

        val cache = mutableMapOf<String, Set<String>>()
    }

    private val samePackageDeclNames =
        cache.getOrPut(packageName) {
            resolver.getDeclarationsFromPackage(packageName)
                .filterNot { it.isPrivate() }
                .map { it.simpleName() }
                .toSet()
        }

    private val starPackageNames: Set<String> = initialImports
        .filter { it.endsWith(".*") }
        .map { it.substringBeforeLast(".*") }
        .toSet()

    private val commonImports = initialImports
        .filterNot { it.endsWith(".*") }
        .associateBy { it.substringBeforeLast(".*") }
        .toMutableMap()

    internal fun getImportBody(): String =
        starPackageNames.map { "$it.*" }
            .plus(commonImports.values)
            .joinToString("\n") { "import $it" }

    /**
     * When you need a nested class, you should import its outermost class.
     */
    private fun addImport(import: String): Boolean {
        val importPackageName = import.substringBeforeLast(".")
        val name = import.substringAfterLast(".")

        return when {
            commonImports[name] == import -> true

            commonImports[name] != null -> false

            // same package > auto-imported > star
            importPackageName == packageName -> true

            importPackageName in AutoImportedPackageNames -> name !in samePackageDeclNames

            importPackageName in starPackageNames ->
                name !in samePackageDeclNames && name !in autoImportedDeclNames

            name in samePackageDeclNames || name in autoImportedDeclNames -> false

            else -> {
                commonImports[name] = import
                true
            }
        }
    }

    public val KSDeclaration.text: String get() =
        if (isLocal())
            simpleName()
        else {
            val outermostPath = outermostDeclaration.qualifiedName()!!

            if (addImport(outermostPath))
                noPackageName()!!
            else
                qualifiedName()!!
        }

    public val KSType.text: String
        get() = buildString {
            append(declaration.text)

            if (arguments.any())
                arguments.joinToString(prefix = "<", postfix = ">", separator = ", ") {
                    when (it.variance) {
                        Variance.STAR -> "*"
                        Variance.INVARIANT -> it.type!!.resolve().text
                        else -> it.variance.label + " " + it.type!!.resolve().text
                    }
                }
                .let(::append)

            if (this@text.isMarkedNullable)
                append("?")
        }

    public val KSTypeReference.text: String get() = resolve().text

    public val KClass<*>.text: String get() =
        if (qualifiedName == null)
            simpleName ?: error("Anonymous object is not allowed here.")
        else
            resolver.getClassDeclarationByName(qualifiedName!!)
            ?.text
            ?: error("$qualifiedName is not imported in the dest module.")
}