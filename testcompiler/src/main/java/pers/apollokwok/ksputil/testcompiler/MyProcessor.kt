package pers.apollokwok.ksputil.testcompiler

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSAnnotated
import pers.apollokwok.ksputil.Environment
import pers.apollokwok.ksputil.KspProcessor
import pers.apollokwok.ksputil.createFile

internal object MyProcessor : KspProcessor {
    override fun process(times: Int): List<KSAnnotated> {
        if (times == 1){
            Environment.codeGenerator.createFile(
                null, "A", Dependencies.ALL_FILES, "class A"
            )
        }
        return emptyList()
    }
}