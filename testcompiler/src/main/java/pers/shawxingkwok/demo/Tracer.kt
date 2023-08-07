package pers.shawxingkwok.demo

annotation class Tracer{
    annotation class Root

    @Target(AnnotationTarget.PROPERTY)
    annotation class Omit
}
