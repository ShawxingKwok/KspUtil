import pers.shawxingkwok.demo.X
import pers.shawxingkwok.demo.XE

interface A {
    fun foo()
}

@X(XE.A, XE.B)
interface B : A{
    override fun foo() {
        TODO("Not yet implemented")
    }
}