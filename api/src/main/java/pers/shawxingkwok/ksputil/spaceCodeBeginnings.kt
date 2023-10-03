package pers.shawxingkwok.ksputil

public fun String.spaceCodeBeginnings(): String{
    var i = 0
    var lastBlockStartsWithValOrVar = false

    return lines().map { it.trimEnd() }
        .joinToString("\n") { line ->
            if (line.isBlank()) return@joinToString ""

            val core = line.trimStart()

            if (core.first() == '}' || core.first() == ')')
                i -= 4

            var n = i

            when {
                core.startsWith("or ")
                || core.startsWith("and ")
                || core.startsWith("xor ")
                || core.startsWith(".")
                || core.startsWith("?.")
                || core.startsWith("::")
                || core.startsWith("->")
                || core.startsWith("|| ")
                || core.startsWith("&& ")
                ->
                    if (lastBlockStartsWithValOrVar) n += 4

                else -> {
                    lastBlockStartsWithValOrVar = false

                    if (core.startsWith(": ")) {
                        check(core.last() != '{'){
                            TODO("")
                        }
                        n += 4
                    }
                }
            }

            if (line.last().let { it == '{' || it == '(' })
                i += 4

            if (core.startsWith("val ") || core.startsWith("var "))
                lastBlockStartsWithValOrVar = true

            check(i >= 0){ "You didn't obey my kt code format." }
            " ".repeat(n) + core
        }
}