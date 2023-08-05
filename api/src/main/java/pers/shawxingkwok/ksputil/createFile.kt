package pers.shawxingkwok.ksputil

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies

// require ksp 1.0.7 or +
@Synchronized
// use 'synchronized' because codeGenerator contains a non-concurrent map.
// todo: undo 'synchronized' after authoritative fix.
/**
 * @param packageName there is no package for the generated file if null or empty
 */
public fun CodeGenerator.createFile(
    packageName: String?,
    fileName: String,
    dependencies: Dependencies,
    content: String,
    extensionName: String = "kt",
) {
    when {
        packageName == null || packageName.none() -> createNewFileByPath(dependencies, fileName, extensionName)
        packageName.isBlank() -> error("Package name consists of all spaces.")
        else -> createNewFile(dependencies, packageName, fileName, extensionName)
    }.run {
        write(content.toByteArray())
        close()
    }
}