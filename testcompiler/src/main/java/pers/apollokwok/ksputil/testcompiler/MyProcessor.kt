package pers.apollokwok.ksputil.testcompiler

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSAnnotated
import pers.apollokwok.ksputil.*
import java.lang.Exception

internal object MyProcessor : KspProcessor {
    override fun process(times: Int): List<KSAnnotated> {
        if (times == 1){
            foo()
        }
        return emptyList()
    }

    fun foo(){
        val packageName = "fs"

        val decls  =
            listOf(
                "pers.apollokwok.testcode.String.A",
                "pers.apollokwok.testcode.A.String",
                "pers.apollokwok.testcode.Tracer",
            )
            .map { resolver.getClassDeclarationByName(it) ?: error(it) }

        val imports = Imports(packageName, decls, Tracer.Root::class)

        var i = 0
        val newDecls = decls
            .joinToString("\n") {klass ->
                "lateinit var ${klass.simpleName().replaceFirstChar { it.lowercase() }}${i++}: ${imports.getName(klass)}"
            }

        val content =
        """
        |package $packageName
                        
        |$imports
            
        |@Tracer.Root    
        |$newDecls
        """.trimMargin()

        Environment.codeGenerator.createFile(
            packageName,
            "x",
            Dependencies.ALL_FILES,
            content = content,
        )
    }
}