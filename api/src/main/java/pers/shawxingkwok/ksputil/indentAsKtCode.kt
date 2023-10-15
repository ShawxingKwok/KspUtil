package pers.shawxingkwok.ksputil

public fun String.indentAsKtCode(): String{
    var tabsSize = 0

    return lines().map { it.trim() }
        .joinToString("\n") { line ->
            if (line.none() || line == "|") return@joinToString ""

            if (line.startsWith("}")
                || line.startsWith(")")
            )
                tabsSize--

            if (line.startsWith("~")) tabsSize++

            buildString {
                if (line.startsWith(": ")) append("    ")

                // incorrect but helps check
                if (tabsSize < 0) tabsSize = 0

                append(" ".repeat(4 * tabsSize))

                val chars = line.toMutableList()
                if (chars.first() == '~')
                    chars.removeFirstOrNull()

                if (chars.takeLast(2) == "!~".toList()) {
                    chars.removeLast()
                    chars.removeLast()
                }

                append(chars.joinToString(""))
            }
            .also {
                when{
                    line.endsWith("{")
                    || line.endsWith("(") -> tabsSize++

                    line.endsWith("!~") -> tabsSize--

                    line.endsWith("->") -> {
                        var x = 0
                        line.reversed().forEach {
                            if (it == '}') x++
                            if (it == '{' && x-- == 0) {
                                tabsSize++
                                return@also
                            }
                        }
                    }
                }
            }
        }
}