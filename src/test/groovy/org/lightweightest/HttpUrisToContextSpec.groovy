package org.lightweightest

import groovy.util.logging.Slf4j
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

@Slf4j
class HttpUrisToContextSpec extends Specification {
    @Shared
    Lightweightest server

    def setupSpec() {
        log.debug("Server about to start...")
        server = Lightweightest.start(port: 9999) {}
    }

    @Unroll
    def "should map request URI to context paths (#uri, #response)"() {
        given:
        server.get("/") { req, resp -> ">>" }
        server.get("/x") { req, resp -> ">>x" }
        server.get("/x/y") { req, resp -> ">>x/y" }
        server.get("/x/z") { req, resp -> ">>x/z" }
        server.get("/y") { req, resp -> ">>y" }
        server.get("/y/z") { req, resp -> ">>y/z" }
        server.get("/x/y/z") { req, resp -> ">>x/y/z" }

        expect:
        "http://localhost:9999${uri}".toURL().text == response

        where:
        uri        || response
        "/x"       || ">>x"
        "/x/y"     || ">>x/y"
        "/x/z"     || ">>x/z"
        "/y"       || ">>y"
        "/y/z"     || ">>y/z"
        "/x/y/z"   || ">>x/y/z"
        // TODO why does it fail?
        // e.g https://docs.oracle.com/javase/8/docs/jre/api/net/httpserver/spec/com/sun/net/httpserver/HttpServer.html
        "/x/y/z/a" || ">>x/y/z"
        "/a"       || ">>"
    }

    @Unroll
    def "should map request URI to context paths (#uri, #response) - based on HttpServer doc"() {
        // TODO this fails...
        // e.g https://docs.oracle.com/javase/8/docs/jre/api/net/httpserver/spec/com/sun/net/httpserver/HttpServer.html
        //
        // If I understand that docs for various paths various contexts should be created. However there is only one
        // context created for "/" path...
        // so we should change teh server code to actually create various contexts...
        given:
        server.get("/") { req, resp -> ">>" }
        server.get("/apps") { req, resp -> ">>apps" }
        server.get("/apps/foo") { req, resp -> ">>apps/foo" }

        expect:
        "http://localhost:9999${uri}".toURL().text == response

        where:
        uri             || response
        "/apps/foo/bar" || ">>apps/foo"
        "/apps/Foo/bar" || "error"
        "/apps/app1"    || ">>apps"
        "/foo"          || ">>"


    }

    def cleanupSpec() {
        log.debug("Server about to stop...")
        server.stop()
    }

}