package co.there4.hexagon.web.integration

import co.there4.hexagon.web.*
import co.there4.hexagon.web.jetty.JettyServer
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import org.asynchttpclient.Response
import java.net.URL

@Test class BlacksheepIT {
    val client: Client by lazy { Client (URL ("http://localhost:${server.bindPort}")) }

    @BeforeClass fun startServers () {
        stop()
        server = JettyServer(bindPort = 8888)

        get ("/books/{id}") {
            ok ("${request ["id"]}:${request.body}")
        }
        get ("/books/{id}/{title}") { ok ("${request ["id"]}:${request ["title"]} ${request.body}") }
        trace ("/books/{id}/{title}") { ok ("${request ["id"]}:${request ["title"]} ${request.body}") }
        patch ("/books/{id}/{title}") { ok ("${request ["id"]}:${request ["title"]} ${request.body}") }
        head ("/books/{id}/{title}") {
            response.addHeader("id", request["id"])
            response.addHeader("title", request["title"])
        }

        run()
        Thread.sleep(1000)
    }

    @AfterClass fun stopServers () {
        stop()
    }

    fun foo () {
        assertResponseContains (client.get ("/books/101"), 200, "101")
    }

    fun getBook () {
        assertResponseContains (client.get ("/books/101/Hamlet"), 200, "101", "Hamlet")
        assertResponseContains (client.trace ("/books/101/Hamlet"), 200, "101", "Hamlet")
        assertResponseContains (client.patch ("/books/101/Hamlet"), 200, "101", "Hamlet")
        assertResponseContains (client.head ("/books/101/Hamlet"), 200)

        assertResponseContains (client.get ("/books/101/Hamlet", "body"), 200, "101", "Hamlet", "body")
        assertResponseContains (client.trace ("/books/101/Hamlet", "body"), 200, "101", "Hamlet", "body")
        assertResponseContains (client.patch ("/books/101/Hamlet", "body"), 200, "101", "Hamlet", "body")
        assertResponseContains (client.head ("/books/101/Hamlet", "body"), 200)
    }

    private fun assertResponseContains(response: Response?, status: Int, vararg content: String) {
        assert (response?.statusCode == status)
        content.forEach {
            assert (response?.responseBody?.contains (it) ?: false)
        }
    }
}