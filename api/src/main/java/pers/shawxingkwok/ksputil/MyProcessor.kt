package pers.shawxingkwok.ksputil

import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import pers.shawxingkwok.ktutil.updateIf

internal object MyProcessor : KSProcessor{
    override fun process(round: Int): List<KSAnnotated> {
        if (round >= 1) return emptyList()

        val processorKSClasses = resolver.getAnnotatedSymbols<Provide, KSClassDeclaration>()

        if (processorKSClasses.none()) return emptyList()

        processorKSClasses.forEach { ksclass ->
            // todo add msg
            require(ksclass.getAllSuperTypes().any { it.declaration.qualifiedName() ==  KSProcessor::class.qualifiedName })
            require(ksclass.classKind == ClassKind.OBJECT)
        }

        processorKSClasses.forEach { processorKSClass ->
            val providerName = processorKSClass.simpleName() + "Provider"

            Environment.codeGenerator.createFile(
                packageName = processorKSClass.packageName(),
                fileName = providerName,
                dependencies = Dependencies(false, processorKSClass.containingFile!!),
                content = """
                    import ${KSProcessorProvider::class.qualifiedName}
                                        
                    internal class $providerName : ${KSProcessorProvider::class.simpleName}({ ${processorKSClass.simpleName()} })
                """.trimIndent()
                    .updateIf({ processorKSClass.packageName().any() }){
                        "package ${processorKSClass.packageName()}\n\n$it"
                    }
            )
        }

        Environment.codeGenerator.createFile(
            packageName = "META-INF.services",
            fileName = SymbolProcessorProvider::class.qualifiedName!!,
            dependencies = Dependencies(false, *processorKSClasses.map { it.containingFile!! }.toTypedArray()),
            content = processorKSClasses.joinToString("\n") { it.qualifiedName()!! + "Provider" },
            extensionName = "",
        )

        return emptyList()
    }

    internal class MyProvider : KSProcessorProvider({ MyProcessor })
}