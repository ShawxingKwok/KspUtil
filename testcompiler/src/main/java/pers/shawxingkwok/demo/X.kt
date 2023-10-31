package pers.shawxingkwok.demo

import kotlin.reflect.KClass

enum class XE{
    A, B
}
annotation class X(vararg val sa: XE)