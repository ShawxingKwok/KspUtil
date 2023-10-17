package pers.shawxingkwok.ksputil

import pers.shawxingkwok.ktutil.allDo
import pers.shawxingkwok.ktutil.updateIf

internal fun String.indentAsKtCode(shrinksEmptyBracketsAndLambdas: Boolean): String{
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
        .updateIf({ shrinksEmptyBracketsAndLambdas }) { text ->
            val arr = mutableListOf<Pair<Int, Int>>()
            var i: Int? = null

            allDo('{' to '}', '(' to ')'){
                (start, end) ->

                text.forEachIndexed { j, c ->
                    when(c){
                        start -> i = j

                        ' ', '\n' -> {}

                        end ->
                            if (i != null) {
                                arr += i!! to j
                                i = null
                            }

                        else -> i = null
                    }
                }
            }

            if (arr.none())
                text
            else
                text.mapIndexed { index, c ->
                    val (start, end) = arr.firstOrNull() ?: return@mapIndexed true

                    val isInnerBlank = index in (start + 1)..< end

                    if (index == end)
                        arr.removeFirstOrNull()

                    when{
                        isInnerBlank -> ""
                        index == end && c == '}' -> " }"
                        else -> "$c"
                    }
                }
                .joinToString("")
        }
}