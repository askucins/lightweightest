package org.lightweightest.uritoctx

import groovy.util.logging.Slf4j
import org.lightweightest.Lightweightest
import spock.lang.*

@IgnoreIf({!env.runjavadocspecs})
@Slf4j
@Title("Mapping request URIs to HttpContext paths")
@Narrative("e.g. https://docs.oracle.com/javase/8/docs/jre/api/net/httpserver/spec/com/sun/net/httpserver/HttpServer.html")
class HttpUrisToContextSpec extends Specification {
    @Shared
    Lightweightest server

    def setupSpec() {
        log.debug("Server about to start...")
        server = Lightweightest.start(port: 9999) {}
    }

    @Unroll
    def "should map request URI to context paths (#uri, #response) - based on HttpServer doc"() {
        given:
        server.get("/") { req, resp -> "ctx:/" }
        server.get("/apps/") { req, resp -> "ctx:/apps/" }
        server.get("/apps/foo/") { req, resp -> "ctx:/apps/foo/" }

        expect:
        "http://localhost:9999${uri}".toURL().text == response

        where:
        uri             || response
        "/apps/foo/bar" || "ctx:/apps/foo/"
        "/apps/app1"    || "ctx:/apps/"
        "/foo"          || "ctx:/"
        "/apps/Foo/bar" || "error"  //TODO this doesn't fail...
    }

    @Unroll
    def "should map request URI to context paths (#uri, #response)"() {
        given:
        server.get("/") { req, resp -> "ctx:/" }
        server.get("/x/") { req, resp -> "ctx:/x/" }
        server.get("/x/y/") { req, resp -> "ctx:/x/y/" }
        server.get("/x/z/") { req, resp -> "ctx:/x/z/" }
        server.get("/y/") { req, resp -> "ctx:/y/" }
        server.get("/y/z/") { req, resp -> "ctx:/y/z/" }
        server.get("/x/y/z/") { req, resp -> "ctx:/x/y/z/" }

        expect:
        "http://localhost:9999${uri}".toURL().text == response

        //TODO review trailing slashes
        where:
        uri        || response
        "/x"       || "ctx:/x/"
        "/x/y"     || "ctx:/x/y/"
        "/x/z"     || "ctx:/x/z/"
        "/y"       || "ctx:/y/"
        "/y/z"     || "ctx:/y/z/"
        "/x/y/z"   || "ctx:/x/y/z/"
        "/x/y/z/a" || "ctx:/x/y/z/"
        "/a"       || "ctx:/"
    }

    def cleanupSpec() {
        log.debug("Server about to stop...")
        server.stop()
    }

}