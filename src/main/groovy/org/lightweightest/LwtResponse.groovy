package org.lightweightest

import com.sun.net.httpserver.Headers
import groovy.util.logging.Slf4j

@Slf4j
class LwtResponse {
    Headers headers
    int status

    public LwtResponse(int status, Headers headers) {
        this.headers = headers
        this.status = status
    }

}
