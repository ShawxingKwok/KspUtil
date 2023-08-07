package pers.shawxingkwok.ksputil

import com.google.devtools.ksp.symbol.KSClassDeclaration
import pers.shawxingkwok.ktutil.updateIf
import kotlin.reflect.KClass

/**
 * annotation > no package > other package > same package > auto-imported package
 * @param annotations are all imported
 * @param ksclasses are partially imported
 */
public class Imports (
    private val packageName: String,
    ksclasses: Collection<KSClassDeclaration>,
    vararg annotations: KClass<out Annotation>,
){
    // the small probability `annotation names conflict` is omitted
    private val displayedMap: Map<String, String> = kotlin.run {
        val ksclassMap = ksclasses
            .sortedBy { ksclass ->
                when{
                    ksclass.packageName().none() -> 3
                    ksclass.packageName() in AutoImportedPackageNames -> 1
                    else -> 2
                }
            }
            .associate { ksclass ->
                val simpleName = ksclass.outermostDeclaration.simpleName()
                val qualifiedName = ksclass.outermostDeclaration.qualifiedName()!!.takeUnless{
                    ksclass.packageName() == packageName
                    || ksclass.packageName() in AutoImportedPackageNames
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
        (ksclassMap + annotationMap).filterValues { it != null } as Map<String, String>
    }

    private val displayedSimpleNames = displayedMap.keys
    private val displayedQualifiedNames = displayedMap.values.toSet()
    private val samePackageSimpleNames = resolver
        .getDeclarationsFromPackage(packageName)
        .filterIsInstance<KSClassDeclaration>()
        .map { it.outermostDeclaration.simpleName() }
        .toSet()

    public fun getKSClassName(ksclass: KSClassDeclaration): String =
        if (contains(ksclass))
            ksclass.noPackageName()!!
        else
            ksclass.qualifiedName()!!

    private fun contains(ksclass: KSClassDeclaration): Boolean{
        // explicitly imported
        if (ksclass.outermostDeclaration.qualifiedName() in displayedQualifiedNames) return true

        // excluded for simple names
        if (ksclass.outermostDeclaration.simpleName() in displayedSimpleNames) return false

        // implicitly imported for same package
        if (ksclass.packageName() == packageName) return true

        // implicitly imported for auto-imported packages
        return ksclass.outermostDeclaration.simpleName() !in samePackageSimpleNames
               && ksclass.packageName() in AutoImportedPackageNames
    }

    override fun toString(): String =
        displayedQualifiedNames.sorted().joinToString("\n"){ "import $it" }
}