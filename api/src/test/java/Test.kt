internal class MyTest {
    class E
    @kotlin.test.Test
    fun foo(){
        class F
        println(E::class.java.enclosingClass)
        println(F::class.java.enclosingClass)
    }
}