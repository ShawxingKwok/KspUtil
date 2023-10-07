package pers.shawxingkwok.ksputil

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSType
import pers.shawxingkwok.ktutil.updateIf
import java.io.File

/**
 * [packageName] could be empty but not suggested.
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
 * [packageName] could be empty but not suggested.
 * Prefix with "/" in [fileName] works as the additional package.
 * Remember to use [KSType.text] in [getBody].
 */
public fun CodeGenerator.createFileWithKtGen(
    packageName: String,
    fileName: String,
    dependencies: Dependencies,
    header: String? = null,
    additionalImports: List<String> = emptyList(),
    copyPaths: List<String> = emptyList(),
    extensionName: String = "kt",
    getBody: KtGen.() -> String,
) {
    require(additionalImports.none { it.startsWith("import ") }){
        "The beginning `import` is needless."
    }

    val ktGen = KtGen(packageName, additionalImports)
    val codeBody = ktGen.getBody()
    val importBody = ktGen.getImportBody()

    val content = buildString {
        if (header != null)
            append("$header\n\n")

        if (packageName.any())
            append("package $packageName\n\n")

        if (importBody.any())
            append("$importBody\n\n")

        if (codeBody.any()) append(codeBody)
    }

    createFile(packageName, fileName, dependencies, content, extensionName)

    copyPaths.forEach { copyPath ->
        val filePath = (copyPath + "/" + packageName.replace(".", "/") + fileName)
            .replace("//", "/")
            .updateIf({ extensionName.any() }){
                "$it.$extensionName"
            }

        val file = File(filePath)
        if (!file.parentFile.exists()) file.parentFile.mkdirs()
        if (!file.exists()) file.createNewFile()
        file.writeText(content)
    }
}