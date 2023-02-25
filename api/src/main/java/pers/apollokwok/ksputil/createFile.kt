package pers.apollokwok.ksputil

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies

// 1.0.7 or +
@Synchronized
// use 'synchronized' because codeGenerator contains a non-concurrent map.
// todo: undo 'synchronized' after authoritative fix.
/**
 * Integrates [CodeGenerator.createNewFile] and [CodeGenerator.createNewFileByPath] with nullable [packageName],
 * and simplifies with [content].
 */
public fun CodeGenerator.createFile(
    packageName: String?,
    fileName: String,
    dependencies: Dependencies,
    content: String,
    extensionName: String = "kt",
) {
    when (packageName) {
        null -> createNewFileByPath(dependencies, fileName, extensionName)
        else -> createNewFile(dependencies, packageName, fileName, extensionName)
    }.run {
        write(content.toByteArray())
        close()
    }
}