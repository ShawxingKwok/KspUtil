package pers.shawxingkwok.demo

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.getFunctionDeclarationsByName
import com.google.devtools.ksp.getPropertyDeclarationByName
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSAnnotated
import pers.shawxingkwok.ksputil.*

@Provide
internal object MyProcessor : KSProcessor {
    override fun process(round: Int): List<KSAnnotated> {
        if (round == 0) {
            foo()
        }
        return emptyList()
    }
}

fun foo(){
    val aType = resolver.getClassDeclarationByName("pers.shawxingkwok.testcode.A")!!.asStarProjectedType()
    val othersStringType = resolver.getClassDeclarationByName("others.String")!!.asStarProjectedType()
    val aStringType = resolver.getClassDeclarationByName("pers.shawxingkwok.testcode.A.String")!!.asStarProjectedType()
    val stringAType = resolver.getClassDeclarationByName("pers.shawxingkwok.testcode.String.A")!!.asStarProjectedType()
    val omitType = resolver.getClassDeclarationByName("pers.shawxingkwok.testcode.Omit")!!.asStarProjectedType()

    val fooFun = resolver.getFunctionDeclarationsByName("pers.shawxingkwok.testcode.foo", true).first()
    val barProp = resolver.getPropertyDeclarationByName("pers.shawxingkwok.testcode.bar", true)!!

    val innerFooFun = resolver.getFunctionDeclarationsByName("pers.shawxingkwok.testcode.MyClass.foo", true).first()
    val innerBarProp = resolver.getPropertyDeclarationByName("pers.shawxingkwok.testcode.MyClass.bar", true)!!

    Environment.codeGenerator.createFileWithKtGen(
        packageName = "pers.shawxingkwok.testcode",
        fileName = "X",
        dependencies = Dependencies.ALL_FILES,
        initialImports = listOf("others.*"),
    ){
        """
        @${Tracer.Omit::class.text}    
        lateinit var a: ${aType.text}
        lateinit var string: ${othersStringType.text}
        lateinit var aString: ${aStringType.text}
        lateinit var stringA: ${stringAType.text}
        lateinit var omit: ${omitType.text}
        lateinit var _string: ${String::class.text}
        val _bar = ${innerBarProp.text}
        val _foo = ${innerFooFun.text}()
        lateinit var k: ${resolver.getClassDeclarationByName("others.K")?.text}
        """.trim().indentAsKtCode()
    }
}