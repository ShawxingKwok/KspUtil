package pers.shawxingkwok.ksputil

import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration

internal object MyProcessor : KSProcessor{
    override fun process(round: Int): List<KSAnnotated> {
        if (round >= 1) return emptyList()

        val processorKSClasses = resolver.getAnnotatedSymbols<Provide, KSClassDeclaration>()

        if (processorKSClasses.none()) return emptyList()

        processorKSClasses.forEach { ksClass ->
            Log.check(
                condition = ksClass.classKind == ClassKind.OBJECT
                    && ksClass.parentDeclaration == null
                    && ksClass.getAllSuperTypes().any { it.declaration.qualifiedName() ==  KSProcessor::class.qualifiedName },

                symbol = ksClass,
            ){
                "The class annotated with `Provide` should be an object, not nest, and a subclass of `KSProcessor`."
            }
        }

        processorKSClasses.forEach { processorKSClass ->
            val providerName = processorKSClass.simpleName() + "Provider"

            Environment.codeGenerator.createFile(
                packageName = processorKSClass.packageName(),
                fileName = providerName,
                dependencies = Dependencies(false, processorKSClass.containingFile!!),
            ){
                "internal class $providerName : ${KSProcessorProvider::class.text}({ ${processorKSClass.text} })"
            }
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