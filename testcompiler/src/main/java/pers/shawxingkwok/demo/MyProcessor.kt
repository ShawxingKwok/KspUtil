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
            resolver.getClassDeclarationByName("B")!!
                .let {

                    Log.d(null, "18")
                    Log.i(null, "19")
                    Log.w(null, "20")

                    Log.d(it, "")
                    Log.i(it, "")
                    Log.w(it, "")

                    Log.d(listOf(it, it), "")
                    Log.i(listOf(it, it), "")
                    Log.w(listOf(it, it), "")

                    // Log.e(null, "6")
                    // val tr = Throwable("X")
                    // Log.e(it, "6", tr)
                    // Log.f(listOf(it, it), 6)

                }
        }
        return emptyList()
    }
}

@OptIn(Delicate::class)
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

    Environment.codeGenerator.createFile(
        packageName = "pers.shawxingkwok.testcode",
        fileName = "X",
        dependencies = Dependencies.ALL_FILES,
        initialImports = setOf(),
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
        lateinit var anotherString: ${resolver.getClassDeclarationByName("another.String")?.text}
        
        fun main(){
            1.${resolver.getFunctionDeclarationsByName("another.foo", true).first().text}()
            "".${resolver.getFunctionDeclarationsByName("others.foo", true).first().text}()
        }
        """
    }
}