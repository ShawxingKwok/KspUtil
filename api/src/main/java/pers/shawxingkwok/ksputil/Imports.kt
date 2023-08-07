package pers.shawxingkwok.ksputil

import com.google.devtools.ksp.symbol.KSClassDeclaration
import pers.shawxingkwok.ktutil.updateIf
import kotlin.reflect.KClass

/**
 * annotation > no package > other package > same package > auto-imported package
 * @param annotations are all imported
 * @param ksClasses are partially imported
 */
public class Imports (
    private val packageName: String,
    ksClasses: Collection<KSClassDeclaration>,
    vararg annotations: KClass<out Annotation>,
){
    // the small probability `annotation names conflict` is omitted
    private val displayedMap: Map<String, String> = kotlin.run {
        val ksClassMap = ksClasses
            .sortedBy { ksClass ->
                when{
                    ksClass.packageName().none() -> 3
                    ksClass.packageName() in AutoImportedPackageNames -> 1
                    else -> 2
                }
            }
            .associate { ksClass ->
                val simpleName = ksClass.outermostDeclaration.simpleName()
                val qualifiedName = ksClass.outermostDeclaration.qualifiedName()!!.takeUnless{
                    ksClass.packageName() == packageName
                    || ksClass.packageName() in AutoImportedPackageNames
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
        (ksClassMap + annotationMap).filterValues { it != null } as Map<String, String>
    }

    private val displayedSimpleNames = displayedMap.keys
    private val displayedQualifiedNames = displayedMap.values.toSet()
    private val samePackageSimpleNames = resolver
        .getDeclarationsFromPackage(packageName)
        .filterIsInstance<KSClassDeclaration>()
        .map { it.outermostDeclaration.simpleName() }
        .toSet()

    public fun getKSClassName(ksClass: KSClassDeclaration): String =
        if (contains(ksClass))
            ksClass.noPackageName()!!
        else
            ksClass.qualifiedName()!!

    private fun contains(ksClass: KSClassDeclaration): Boolean{
        // explicitly imported
        if (ksClass.outermostDeclaration.qualifiedName() in displayedQualifiedNames) return true

        // excluded for simple names
        if (ksClass.outermostDeclaration.simpleName() in displayedSimpleNames) return false

        // implicitly imported for same package
        if (ksClass.packageName() == packageName) return true

        // implicitly imported for auto-imported packages
        return ksClass.outermostDeclaration.simpleName() !in samePackageSimpleNames
               && ksClass.packageName() in AutoImportedPackageNames
    }

    override fun toString(): String =
        displayedQualifiedNames.sorted().joinToString("\n"){ "import $it" }
}