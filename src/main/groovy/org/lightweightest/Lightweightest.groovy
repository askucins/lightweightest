package org.lightweightest

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import groovy.util.logging.Slf4j

import java.util.concurrent.CountDownLatch

@Slf4j
class Lightweightest {
    HttpServer server
    CountDownLatch latch = null
    def methods = ['GET': [:], 'POST': [:]]
    def requests = []

    public Lightweightest() {
    }

    void init(def params) {
        if (params.stopAfter) {
            latch = new CountDownLatch(params.stopAfter)
        }
        InetSocketAddress addr = new InetSocketAddress(params.port)
        server = HttpServer.create(addr, 0)
        server.createContext("/", new HttpHandler() {
            @Override
            void handle(HttpExchange exchange) throws Exception {
                // based on an advice from:
                // http://stackoverflow.com/questions/26815752/create-dynamically-contexts-for-com-sun-net-httpserver-httpserver-java
                //def func = methods[exchange.requestMethod][exchange.requestURI.path.toString()]
                def func = findFunc(exchange.requestMethod, exchange.requestURI.path.toString())
                def request = new LwtRequest(exchange.requestURI, exchange.requestBody.bytes, exchange.requestHeaders)
                requests << request
                def response = new LwtResponse(200, exchange.getResponseHeaders())
                response.headers.set("Content-Type", "text/plain")
                try {
                    def result = func(request, response)
                    exchange.sendResponseHeaders(response.status, 0)
                    exchange.responseBody << result
                } catch (e) {
                    e.printStackTrace()
                    StringWriter writer = new StringWriter()
                    exchange.sendResponseHeaders(500, 0)
                    e.printStackTrace(new PrintWriter(writer))
                    exchange.responseBody << writer.toString()
                }
                exchange.responseBody.close()
                if (latch) {
                    latch.countDown()
                    if (latch.count < 1) {
                        Lightweightest.this.stop()
                    }
                }
            }
        })
    }

    private Closure findFunc(String requestMethod, String path) {
        def context = methods[requestMethod].keySet().findAll { String ctx ->
            path.startsWith(ctx) }?.max { a, b -> a.size() <=> b.size() }
        if (context == null) {
            //TODO return 404
            return null
        } else {
            return methods[requestMethod][context]
        }
    }

    public void get(String str, Closure closure) {
        methods['GET'][str] = closure
    }

    public void post(String str, Closure closure) {
        methods['POST'][str] = closure
    }

    public void start() {
        server.start()
    }

    public void stop() {
        server.stop(0)
    }

    public static Lightweightest start(Map params, Closure config) {
        Lightweightest server = new Lightweightest()
        if (config) {
            config.setDelegate(server)
            config()
        }
        server.init(params)
        server.start()
        return server
    }

}