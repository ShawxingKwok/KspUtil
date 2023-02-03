package pers.apollokwok.ksputil.testcompiler.test

import com.google.devtools.ksp.symbol.KSAnnotated
import pers.apollokwok.ksputil.KspProcessor
import pers.apollokwok.ksputil.Log

internal class TestProcessor :  KspProcessor{
    override fun process(times: Int): List<KSAnnotated> {
        Log.w("In ${javaClass.simpleName}")
        return emptyList()
    }
}