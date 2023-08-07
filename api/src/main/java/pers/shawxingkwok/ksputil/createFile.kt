package pers.shawxingkwok.ksputil

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies

/**
 * [fileName] could be used as path when [packageName] is `null`.
 */
@Synchronized
// use 'synchronized' because codeGenerator contains a non-concurrent map.
// todo: undo 'synchronized' after authoritative fix.
public fun CodeGenerator.createFile(
    packageName: String?,
    fileName: String,
    dependencies: Dependencies,
    content: String,
    extensionName: String = "kt",
) {
    createNewFile(
        dependencies = dependencies,
        packageName = packageName ?: "",
        fileName = fileName,
        extensionName = extensionName
    ).run {
        write(content.toByteArray())
        close()
    }
}