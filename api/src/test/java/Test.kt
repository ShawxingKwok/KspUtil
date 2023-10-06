import pers.shawxingkwok.ksputil.indentAsKtCode

internal class MyTest {
    class E
    @kotlin.test.Test
    fun foo(){
        """
            internal class AccountApiImpl(private val client: HttpClient) : AccountApi { 
                companion object {
                   private const val HOST = "127.0.0.0" 
                   val x = listOf(1)
                       ~.filter{ true }!~     
                }
                
                override suspend fun login(
                    email: String,
                    password: String,
                    verificationCode: String,
                ): LoginResult =
                    ~client.post("https://$${"HOST"}/login"){
                        contentType(ContentType.Application.FormUrlEncoded)

                        Parameters.build {
                            append("email", email)
                            append("password", password)
                            append("verificationCode", verificationCode)
                        }
                        .let(::setBody)
                    }
                    .body()!~

            override suspend fun delete(id: String) {
                client.delete("https://$${"HOST"}/delete"){
                    parameter("id", id)
                }
            }

            override suspend fun search(id: String): User? {
                return client.get("https://$${"HOST"}/search"){
                    parameter("id", id)
                }
                .body()
            }
        }
        """.trimIndent()
            .indentAsKtCode()
            .let(::println)
    }
}