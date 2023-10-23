interface A {
    fun foo()
}

@X<List<String>>
interface B : A{
    override fun foo() {
        TODO("Not yet implemented")
    }
}

annotation class X<T>