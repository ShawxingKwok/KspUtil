package pers.shawxingkwok.ksputil

private val beginnings = "{ (".split(" ").map { it.first() }
private val endings = "} )".split(" ").map { it.first() }

public fun String.spaceCodeBeginnings(): String{
    var i = 0

    return lines().map { it.trimEnd() }
        .joinToString("\n") { line ->
            if (line.isBlank()) return@joinToString ""

            fun getNewLine() = " ".repeat(i) + line.trimStart()

            when {
                line.last() in beginnings ->
                    getNewLine().also {
                        i += 4
                    }

                line.trimStart().length == 1 && line.last() in endings -> {
                    i -= 4
                    getNewLine()
                }

                line.trimStart().startsWith(": ") -> " ".repeat(4) + getNewLine()

                else -> getNewLine()
            }
        }
}