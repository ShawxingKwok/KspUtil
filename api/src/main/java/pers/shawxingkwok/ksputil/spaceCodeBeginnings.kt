package pers.shawxingkwok.ksputil

public fun String.spaceCodeBeginnings(): String{
    var tabsSize = 0

    return lines().map { it.trim() }
        .joinToString("\n") { line ->
            if (line.none()) return@joinToString ""

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

                if (chars.last() == '~')
                    chars.removeLastOrNull()

                append(chars.joinToString(""))
            }
            .also {
                if (line.endsWith("{")
                    || line.endsWith("(")
                )
                    tabsSize++

                if (line.endsWith("~")) tabsSize--
            }
        }
}