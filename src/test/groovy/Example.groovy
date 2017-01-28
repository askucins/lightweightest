@GrabResolver(name = "lightweightest", m2Compatible = 'true', root = 'https://raw.github.com/askucins/lightweightest/develop/repository')
@Grab("org.lightweightest:lightweightest:0.5")
import org.lightweightest.Lightweightest

def server = Lightweightest.start(port: 9999) {
    get("/card/list") { request, response ->
        "qwerty"
    }
}

server.get("/test") { request, response ->
    "qwerty ${request.params.id}"
}

println "http://localhost:9999/card/list".toURL().text
println "http://localhost:9999/test?id=1001".toURL().text


server.stop()