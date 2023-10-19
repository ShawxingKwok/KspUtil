package pers.shawxingkwok.ksputil

internal fun String.formatAsCode(): String{
    var tabsSize = 0

    return lines()
        .map { it.trim() }
         // indent rightly
        .map { line ->
            if (line.none()) return@map line

            if (line.startsWith("}")
                || line.startsWith(")")
            )
                tabsSize--

            if (line.startsWith("~")) tabsSize++

            buildString {
                if (line.startsWith(": ")) append("    ")

                // is incorrect but helps check
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
            }
        }
        .toMutableList()
        // remove redundant blank lines
        .also { lines ->
            var i = 1

            while (true){
                val last = lines.getOrNull(i - 1) ?: break
                val trimmedCurrent = lines.getOrNull(i)?.trimStart() ?: break
                val trimmedNext = lines.getOrNull(i + 1)?.trimStart() ?: break

                when{
                    trimmedCurrent.isNotBlank() ->
                        if (last.endsWith("{") && trimmedCurrent.startsWith("}")
                            || last.endsWith("(") && trimmedCurrent.startsWith(")")
                        ){
                            lines[i - 1] = "$last$trimmedCurrent"
                            lines.removeAt(i)
                        }else
                            i++

                    last.endsWith("{")
                    || last.endsWith("(")
                    || trimmedNext.startsWith("}")
                    || trimmedNext.startsWith(")")
                    || trimmedNext.isBlank()
                        -> lines.removeAt(i)

                    else -> i++
                }
            }
        }
        .joinToString("\n")
}