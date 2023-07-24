package pers.shawxingkwok.ksputil

import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration

internal object MyProcessor : KSProcessor{
    override fun process(times: Int): List<KSAnnotated> {
        if (times != 1) return emptyList()

        val processorDecls = resolver.getAnnotatedSymbols<Provide, KSClassDeclaration>()

        if (processorDecls.none()) return emptyList()

        processorDecls.forEach { klassDecl ->
            // todo add msg
            require(klassDecl.getAllSuperTypes().any { it.declaration.qualifiedName() ==  KSProcessor::class.qualifiedName })
            require(klassDecl.classKind == ClassKind.OBJECT)
        }

        processorDecls.forEach { processorDecl ->
            val providerName = processorDecl.simpleName() + "Provider"

            Environment.codeGenerator.createFile(
                packageName = processorDecl.packageName(),
                fileName = providerName,
                dependencies = Dependencies(false, processorDecl.containingFile!!),
                content = """
                    package ${processorDecl.packageName()}
                    
                    import ${KSProcessorProvider::class.qualifiedName}
                                        
                    internal class $providerName : ${KSProcessorProvider::class.simpleName}({ ${processorDecl.simpleName()} })
                """.trimIndent()
            )
        }

        Environment.codeGenerator.createFile(
            packageName = "META-INF.services",
            fileName = SymbolProcessorProvider::class.qualifiedName!!,
            dependencies = Dependencies(false, *processorDecls.map { it.containingFile!! }.toTypedArray()),
            content = processorDecls.joinToString("\n") { it.qualifiedName()!! + "Provider" },
            extensionName = "",
        )

        return emptyList()
    }

    internal class MyProvider : KSProcessorProvider({ MyProcessor })
}