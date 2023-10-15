package pers.shawxingkwok.ksputil

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.isPrivate
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.symbol.*
import kotlin.reflect.KClass

public class KtGen internal constructor(
    private val packageName: String,
    initialImports: Set<String>,
) {
    private companion object {
        val autoImportedDeclNames = AutoImportedPackageNames
            .flatMap { resolver.getDeclarationsFromPackage(it) }
            .filter { it.isPublic() }
            .map { it.simpleName() }
            .toSet()

        val packageDeclNamesCache = mutableMapOf<String, Set<String>>()
    }

    private val samePackageDeclNames =
        packageDeclNamesCache.getOrPut(packageName) {
            resolver.getDeclarationsFromPackage(packageName)
                .filterNot { it.isPrivate() }
                .map { it.simpleName() }
                .toSet()
        }

    private val starPackageNames: Set<String> = initialImports
        .filter { it.endsWith(".*") }
        .map { it.substringBeforeLast(".*") }
        .toSet()

    private val starPackageDeclNames =
        starPackageNames
        .flatMap { packageName ->
            packageDeclNamesCache.getOrPut(packageName) {
                resolver.getDeclarationsFromPackage(packageName)
                    .filter { it.isPublic() }
                    .map { it.simpleName() }
                    .toSet()
            }
        }
        .toSet()

    private val commonImports = initialImports
        .filterNot { it.endsWith(".*") }
        .associateBy { it.substringBeforeLast(".*") }
        .toMutableMap()

    private val renamedImports = mutableMapOf<String, String>()
    private val renamedIndices = mutableMapOf<String, Int>()

    internal fun getImportBody(): String =
        starPackageNames.map { "$it.*" }
            .plus(commonImports.values)
            .plus(renamedImports.map { "${it.key} as ${it.value}" })
            .joinToString("\n") { "import $it" }

    /**
     * When you need a nested or inner declaration, you should import its outermost class.
     */
    public fun getDeclText(
        import: String,
        innerName: String?,
        isTopLevelAndExtensional: Boolean,
    ): String {
        require("." in import){
            "Found empty package name which takes much effort to adapt. And it could appear only in tests."
        }
        if (import == "another.foo")
            Log.d("$import $innerName $isTopLevelAndExtensional")

        val importPackageName = import.substringBeforeLast(".")
        val importSuffix = import.substringAfterLast(".")
        val fullName = listOfNotNull(import, innerName).joinToString(".")
        val directName = listOfNotNull(importSuffix, innerName).joinToString(".")

        fun renameIfTopLevelAndExtensional(): String =
            if (isTopLevelAndExtensional)
                renamedImports.getOrPut(import){
                    val i = renamedIndices[importSuffix]?.plus(1) ?: 1
                    renamedIndices[importSuffix] = i
                    directName + i
                }
            else
                fullName

        return when {
            commonImports[importSuffix] == import -> directName

            commonImports[importSuffix] != null -> renameIfTopLevelAndExtensional()

            // same package > auto-imported > star
            importPackageName == packageName -> directName

            importPackageName in AutoImportedPackageNames ->
                if (importSuffix in samePackageDeclNames) fullName else directName

            importPackageName in starPackageNames ->
                if (importSuffix in samePackageDeclNames || importSuffix in autoImportedDeclNames)
                    fullName
                else
                    directName

            // avoid overriding since users may use pure text to
            // reference callables from those auto-imported.
            importSuffix in samePackageDeclNames
            || importSuffix in autoImportedDeclNames
            || importSuffix in starPackageDeclNames -> renameIfTopLevelAndExtensional()

            else -> {
                commonImports[importSuffix] = import
                directName
            }
        }
    }

    public val KSDeclaration.text: String get() {
        val outermostPath = outermostDeclaration.qualifiedName()!!

        return getDeclText(
            // local declarations are not considered.
            import = outermostPath,
            innerName =
                if (parentDeclaration == null)
                    null
                else
                    qualifiedName()!!.substringAfter("$outermostPath."),
            isTopLevelAndExtensional =
                parentDeclaration == null
                && (this is KSFunctionDeclaration && this.extensionReceiver != null
                || this is KSPropertyDeclaration && this.extensionReceiver != null)
        )
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

    public fun newLineIf(condition: Boolean, getText: () -> String): String =
        if (condition) getText()
        else "|"
}