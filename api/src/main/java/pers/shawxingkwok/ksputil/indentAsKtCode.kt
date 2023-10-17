package pers.shawxingkwok.ksputil

internal fun String.indentAsKtCode(): String{
    var tabsSize = 0

    return lines().map { it.trim() }
        .joinToString("") { line ->
            if (line.none()) return@joinToString "\n"
            if (line == "|") return@joinToString ""

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
                    chars.removeFirst()

                when {
                    line.endsWith("{")
                    || line.endsWith("(") -> tabsSize++

                    line.endsWith("!~") ->
                        while (chars.takeLast(2) == "!~".toList()) {
                            chars.removeLast()
                            chars.removeLast()
                            tabsSize--
                        }

                    line.endsWith("->") -> {
                        var x = 0
                        line.reversed().forEach {
                            if (it == '}') x++
                            if (it == '{' && x-- == 0)
                                tabsSize++
                        }
                    }
                }

                append(chars.joinToString(""))
                append("\n")
            }
        }
        .removeSuffix("\n")
}