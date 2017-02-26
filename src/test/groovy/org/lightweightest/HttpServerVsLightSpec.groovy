package org.lightweightest

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import groovy.util.logging.Slf4j
import spock.lang.*

@Slf4j
@Title("Mapping request URIs to HttpContext paths - compare to bare HttpServer")
@Narrative("e.g. https://docs.oracle.com/javase/8/docs/jre/api/net/httpserver/spec/com/sun/net/httpserver/HttpServer.html")
class HttpServerVsLightSpec extends Specification {

    @Shared
    HttpServer httpServer
    @Shared
    Lightweightest lightServer

    static class TestHandler implements HttpHandler {
        String response

        TestHandler(String response) {
            this.response = response
        }
        // see http://stackoverflow.com/questions/3732109/simple-http-server-in-java-using-only-java-se-api
        @Override
        void handle(HttpExchange exchange) throws IOException {
            exchange.sendResponseHeaders(200, response.length())
            exchange.responseBody << response
            exchange.responseBody.close()
        }
    }

    def setupSpec() {
        log.debug("Light server about to start...")
        lightServer = Lightweightest.start(port: 9999) {}

        log.debug("Http server about to init...")
        httpServer = HttpServer.create(new InetSocketAddress(8888), 0)

        log.debug("About to create handlers...")
        ["/", "/apps/", "/apps/foo/"].each { path ->
            lightServer.get(path) { req, resp -> 'ctx:' + path }
            httpServer.createContext(path, new TestHandler('ctx:' + path))
        }

        log.debug("Http server about to start...")
        httpServer.start()
    }

    def cleanupSpec() {
        lightServer.stop()
        httpServer.stop(0)
    }

    def testCases() {
        [
                ["/apps/foo/bar", "ctx:/apps/foo/"],
                ["/apps/app1", "ctx:/apps/"],
                ["/foo", "ctx:/"],
                ["/apps/Foo/bar", "error"] //TODO contrary to docs this does *not* fail!!
        ]
    }

    @Unroll
    def "should LightServer map request URI to context paths (#uri, #response)"() {
        expect:
        "http://localhost:9999${uri}".toURL().text == response
        where:
        [uri, response] << testCases()
    }

    @Unroll
    def "should HttpServer map request URI to context paths (#uri, #response)"() {
        expect:
        "http://localhost:8888${uri}".toURL().text == response
        where:
        [uri, response] << testCases()
    }
}