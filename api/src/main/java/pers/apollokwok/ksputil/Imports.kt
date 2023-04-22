package pers.apollokwok.ksputil

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import kotlin.reflect.KClass

/**
 * annotation > no package > other package > same package > auto-imported package
 * @param annotations are all imported
 * @param klasses are partially imported
 */
public class Imports constructor(
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

    // probability `annotation names conflict` is omitted
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
            val qualifiedName = annot.qualifiedName!!
                .substringAfter(annot.java.`package`.name + ".")
                .substringBefore(".")
                .takeUnless {
                    annot.java.`package`.name == packageName
                    || annot.java.`package`.name in AutoImportedPackageNames
                }

            annot.simpleName!! to qualifiedName
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

    public operator fun contains(klass: KSClassDeclaration): Boolean{
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