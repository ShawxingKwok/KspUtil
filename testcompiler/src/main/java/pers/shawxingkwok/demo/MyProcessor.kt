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
    val aType = resolver.getClassDeclarationByName("pers.apollokwok.testcode.A")!!.asStarProjectedType()
    val stringType = resolver.getClassDeclarationByName("pers.apollokwok.testcode.String")!!.asStarProjectedType()
    val aStringType = resolver.getClassDeclarationByName("pers.apollokwok.testcode.A.String")!!.asStarProjectedType()
    val stringAType = resolver.getClassDeclarationByName("pers.apollokwok.testcode.String.A")!!.asStarProjectedType()
    val omitType = resolver.getClassDeclarationByName("pers.apollokwok.testcode.Omit")!!.asStarProjectedType()

    val fooFun = resolver.getFunctionDeclarationsByName("pers.apollokwok.testcode.foo", true).first()
    val barProp = resolver.getPropertyDeclarationByName("pers.apollokwok.testcode.bar", true)!!

    val innerFooFun = resolver.getFunctionDeclarationsByName("pers.apollokwok.testcode.MyClass.foo", true).first()
    val innerBarProp = resolver.getPropertyDeclarationByName("pers.apollokwok.testcode.MyClass.bar", true)!!

    Environment.codeGenerator.createKtFile(
        packageName = "fs",
        fileName = "X",
        dependencies = Dependencies.ALL_FILES,
    ){
        """
        |@${Tracer.Omit::class.text}    
        |lateinit var a: ${aType.text}
        |lateinit var string: ${stringType.text}
        |lateinit var aString: ${aStringType.text}
        |lateinit var stringA: ${stringAType.text}
        |lateinit var omit: ${omitType.text}
        |lateinit var _string: ${String::class.text}
        |val bar = ${innerBarProp.text}
        |val foo = ${innerFooFun.text}()
        """.trimMargin()
    }
}