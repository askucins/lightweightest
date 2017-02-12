package org.lightweightest

import groovy.util.logging.Slf4j
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

@Stepwise
@Slf4j
class HttpUrisToContextSpec extends Specification {
    @Shared
    Lightweightest server

    def "start server" () {
        when:
        log.debug("Server started...")
        server = Lightweightest.start(port:9999){}
        then:
        server
//        "http://localhost:9999/test".toURL().text
//        then:
//        thrown(IOException)

    }

    def "stop server"() {
        expect:
        log.debug("Server about to stop...")
        server.stop()

    }
}