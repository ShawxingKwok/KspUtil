package pers.shawxingkwok.demo

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSAnnotated
import pers.shawxingkwok.ksputil.*

@Provide
internal object MyProcessor : KSProcessor {
    override fun process(round: Int): List<KSAnnotated> {
        if (round == 0){
            foo()
        }
        return emptyList()
    }

    fun foo(){
        Environment.codeGenerator.createNewFile(
            dependencies = Dependencies.ALL_FILES,
            packageName = "tm",
            fileName = "fmp/nuie"
        ).run {
            write("// ${System.currentTimeMillis()}".toByteArray())
            close()
        }

        Environment.codeGenerator.createNewFileByPath(Dependencies.ALL_FILES, "fruo/huq").run {
            write("class GNUOFR".toByteArray())
            close()
        }

        val packageName = "fs"

        val decls  =
            listOf(
                "pers.apollokwok.testcode.String.A",
                "pers.apollokwok.testcode.A.String",
                "pers.shawxingkwok.demo.Tracer",
            )
            .map { resolver.getClassDeclarationByName(it) ?: error(it) }

        val imports = Imports(packageName, decls, Tracer::class)

        val newDecls = decls
            .joinToString("\n") {klass ->
                "lateinit var ${klass.simpleName().replaceFirstChar { it.lowercase() }}: ${imports.getKSClassName(klass)}"
            }

        val content =
        """
        |package $packageName
                        
        |$imports
            
        |@Tracer.Omit    
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