package pers.apollokwok.ksputil

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import pers.apollokwok.ktutil.Unreachable
import kotlin.reflect.KClass

public class Imports(
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

    private val imported: Collection<Any> = kotlin.run {
        // associate by their simple names
        val klassMap = klasses.associate { it.outermostDecl.simpleName() to it.outermostDecl }

        val annotationMap = annotations.associateBy { annot ->
            annot.qualifiedName!!
                .substringAfter(annot.java.`package`.name + ".")
                .substringBefore(".")
        }

        (klassMap + annotationMap).values
    }

    private val importedQualifiedNames: Set<String> =
        imported.map {
            when(it){
                is KSClassDeclaration -> it.outermostDecl.qualifiedName()!!
                is KClass<*> -> it.qualifiedName!!
                else -> Unreachable()
            }
        }
        .toSet()

    public operator fun contains(klass: KSClassDeclaration): Boolean =
        klass.outermostDecl.qualifiedName() in importedQualifiedNames

    override fun toString(): String =
        imported.mapNotNull {
            when(it){
                is KSClassDeclaration -> it.outermostDecl.qualifiedName()!!
                    .takeUnless { _->
                        it.packageName() in AutoImportedPackageNames
                        || it.packageName() == packageName
                    }

                is KClass<*> -> it.qualifiedName!!
                    .takeUnless { _->
                        it.java.`package`.name in AutoImportedPackageNames
                        || it.java.`package`.name == packageName
                    }

                else -> Unreachable()
            }
        }
        .sorted()
        .joinToString("\n"){ "import $it" }
}