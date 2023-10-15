package pers.shawxingkwok.ksputil

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSType

/**
 * [packageName] can't be empty.
 * Prefix with "/" in [fileName] works as the additional package.
 */
@Synchronized
// use 'synchronized' because codeGenerator contains a non-concurrent map.
// todo: undo 'synchronized' after authoritative fix.
public fun CodeGenerator.createFile(
    packageName: String,
    fileName: String,
    dependencies: Dependencies,
    content: String,
    extensionName: String = "kt",
) {
    require(packageName.any()){
        "Empty package name is used only in some test cases. " +
        "However, I don't want to spend much effort adapting it."
    }
    createNewFile(
        dependencies = dependencies,
        packageName = packageName,
        fileName = fileName,
        extensionName = extensionName
    ).run {
        write(content.toByteArray())
        close()
    }
}

/**
 * [packageName] can't be empty.
 * Prefix with "/" in [fileName] works as the additional package.
 * Remember to use [KSType.text] in [getBody] of which the output would
 * be trimmed and processed by [indentAsKtCode].
 */
public fun CodeGenerator.createFileWithKtGen(
    packageName: String,
    fileName: String,
    dependencies: Dependencies,
    header: String? = null,
    initialImports: List<String> = emptyList(),
    extensionName: String = "kt",
    getBody: KtGen.() -> String,
) {
    require(initialImports.none { it.startsWith("import ") }){
        "The beginning `import` is needless."
    }

    require(packageName.any()){
        "Empty package name is used only in some test cases. " +
        "However, I don't want to spend much effort adapting it."
    }

    val ktGen = KtGen(packageName, initialImports)
    val codeBody = ktGen.getBody()
    val importBody = ktGen.getImportBody()

    val content = buildString {
        if (header != null)
            append("$header\n\n")

        append("package $packageName\n\n")

        if (importBody.any())
            append("$importBody\n\n")

        if (codeBody.any()) append(codeBody)
    }

    createFile(packageName, fileName, dependencies, content, extensionName)
}