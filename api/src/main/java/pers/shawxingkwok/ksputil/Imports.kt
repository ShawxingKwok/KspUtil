package pers.shawxingkwok.ksputil

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import pers.shawxingkwok.ktutil.updateIf
import kotlin.reflect.KClass

/**
 * annotation > no package > other package > same package > auto-imported package
 * @param annotations are all imported
 * @param klasses are partially imported
 */
public class Imports (
    private val packageName: String,
    klasses: Collection<KSClassDeclaration>,
    vararg annotations: KClass<out Annotation>,
){
    public constructor(
        srcDecl: KSDeclaration,
        klasses: Collection<KSClassDeclaration>,
        vararg annotations: KClass<out Annotation>,
    ) :
        this(
            packageName = srcDecl.packageName(),
            klasses = klasses,
            annotations = annotations,
        )

    // the small probability `annotation names conflict` is omitted
    private val displayedMap: Map<String, String> = kotlin.run {
        val klassMap = klasses
            .sortedBy { klass ->
                when{
                    klass.packageName().none() -> 3
                    klass.packageName() in AutoImportedPackageNames -> 1
                    else -> 2
                }
            }
            .associate { klass ->
                val simpleName = klass.outermostDecl.simpleName()
                val qualifiedName = klass.outermostDecl.qualifiedName()!!.takeUnless{
                    klass.packageName() == packageName
                    || klass.packageName() in AutoImportedPackageNames
                }
                simpleName to qualifiedName
            }

        val annotationMap = annotations.associate { annot ->
            val simpleName = annot.qualifiedName!!
                .updateIf({ annot.java.`package`.name.any() }){
                    it.substringAfter(annot.java.`package`.name + ".")
                }
                .substringBefore(".")

            val qualifiedName =
                when (annot.java.`package`.name) {
                    packageName, in AutoImportedPackageNames -> null
                    "" -> simpleName
                    else -> annot.java.`package`.name + "." + simpleName
                }

            simpleName to qualifiedName
        }

        @Suppress("UNCHECKED_CAST")
        (klassMap + annotationMap).filterValues { it != null } as Map<String, String>
    }

    private val displayedSimpleNames = displayedMap.keys
    private val displayedQualifiedNames = displayedMap.values.toSet()
    private val samePackageSimpleNames = resolver
        .getDeclarationsFromPackage(packageName)
        .filterIsInstance<KSClassDeclaration>()
        .map { it.outermostDecl.simpleName() }
        .toSet()

    public fun getName(klass: KSClassDeclaration): String =
        if (contains(klass))
            klass.noPackageName()!!
        else
            klass.qualifiedName()!!

    private fun contains(klass: KSClassDeclaration): Boolean{
        // explicitly imported
        if (klass.outermostDecl.qualifiedName() in displayedQualifiedNames) return true

        // excluded for simple names
        if (klass.outermostDecl.simpleName() in displayedSimpleNames) return false

        // implicitly imported for same package
        if (klass.packageName() == packageName) return true

        // implicitly imported for auto-imported packages
        return klass.outermostDecl.simpleName() !in samePackageSimpleNames
               && klass.packageName() in AutoImportedPackageNames
    }

    override fun toString(): String =
        displayedQualifiedNames.sorted().joinToString("\n"){ "import $it" }
}