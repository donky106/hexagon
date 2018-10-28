
import com.hexagonkt.client.Client
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test

class ServiceTest {
    private val client by lazy { Client("http://localhost:${server.runtimePort}") }

    companion object {
        @BeforeClass @JvmStatic fun startup() {
            main()
        }

        @AfterClass @JvmStatic fun shutdown() {
            server.stop()
        }
    }

    @Test
    fun httpRequest() {
        val response = client.get("/text")
        val content = response.responseBody

        assert(response.headers ["Date"] != null)
        assert(response.headers ["Server"] != null)
        assert(response.headers ["Transfer-Encoding"] != null)
        assert(response.headers ["Content-Type"] == "text/plain")

        assert("Hello, World!" == content)
    }
}