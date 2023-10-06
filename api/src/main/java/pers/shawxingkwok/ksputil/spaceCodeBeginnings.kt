package pers.shawxingkwok.ksputil

public fun String.spaceCodeBeginnings(): String{
    var i = 0
    var inStatement = false

    return lines().map { it.trimEnd() }
        .joinToString("\n") { line ->
            if (line.isBlank()) return@joinToString ""

            val core = line.trimStart()

            if (core.first() == '}' || core.first() == ')')
                i -= 4

            var n = i

            when {
                arrayOf(
                    "or ", "and ", "xor ",
                    "in ", "as ", "is",
                    "!in ", "as? ", "!is",
                    ".", "?.", "::", "->", "||", "&&",
                    "+", "-", "*", "/", "%",
                    "+=", "-=", "*=", "/=",
                )
                .any(core::startsWith)
                ->
                    if (inStatement) n += 4

                else -> {
                    inStatement = false

                    if (core.startsWith(": "))
                        n += 4
                }
            }

            if (core.startsWith("val ")
                || core.startsWith("var ")
                || core.endsWith(" =")
                || core.endsWith(" by")
            )
                inStatement = true

            if (line.last().let { it == '{' || it == '(' })
                i += 4

            " ".repeat(n) + core
        }
}