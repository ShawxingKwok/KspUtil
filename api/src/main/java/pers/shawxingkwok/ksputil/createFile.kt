package pers.shawxingkwok.ksputil

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSType

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
 * [packageName] can't be empty.
 * Prefix with "/" in [fileName] works as the additional package.
 * Remember to use [KSType.text] in [getBody] of which the output would
 * be trimmed and processed by [formatAsCode].
 */
public fun CodeGenerator.createFileWithKtGen(
    packageName: String,
    fileName: String,
    dependencies: Dependencies,
    header: String? = null,
    initialImports: Set<String> = setOf(),
    extensionName: String = "kt",
    getBody: CodeFormatter.() -> String,
) {
    require(initialImports.firstOrNull()?.startsWith("import ") != true){
        "The beginning `import` is needless."
    }

    require(packageName.any()){
        "Empty package name is used only in some test cases. " +
        "However, I don't want to spend much effort adapting it."
    }

    val codeFormatter = CodeFormatter(packageName, initialImports)
    val codeBody = codeFormatter.getBody()
    val importBody = codeFormatter.getImportBody()

    val content = buildString {
        if (header != null)
            append("$header\n\n")

        append("package $packageName\n\n")

        if (importBody.any())
            append("$importBody\n\n")

        if (codeBody.any())
            append(codeBody.trim().formatAsCode())
    }

    createFile(packageName, fileName, dependencies, content, extensionName)
}