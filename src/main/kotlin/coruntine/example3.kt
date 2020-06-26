package coruntine

import io.vertx.core.AbstractVerticle
import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.Message
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.coroutines.awaitResult
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("log")

private class ServiceVerticle : AbstractVerticle() {
    override fun start() {
        val consumer = vertx.eventBus().localConsumer<String>("a.b")
        consumer.handler {
            log.warn("Consumer received: ${it.body()}")
            val request = it
            vertx.setTimer(3000) { request.reply("this is reply") }
        }
    }
}

private class ApiVerticle : CoroutineVerticle() {
    override suspend fun start() {
        vertx.setPeriodic(2000) { log.info("periodic job.") }
        vertx.eventBus().localConsumer<Int>("a.")
            .handler {
                log.info("ApiVerticle receive request: ${it.body()}")
                GlobalScope.launch(Dispatchers.Unconfined) {
                    log.info("Send a message to Service Verticle and wait for a reply...")
                    // style 1
//                    val reply = awaitResult<Message<String>> { handler ->
//                        vertx.eventBus().request("a.b", "ping", handler)
//                    }
                    // style 2
                    val reply = vertx.eventBus().request<Any>("a.b", "ping").await()


                    log.warn("Reply received: ${reply.body()}")
                }
            }
    }
}

fun main() {
    val vertx = Vertx.vertx()
    vertx.deployVerticle(ServiceVerticle())
        .onSuccess { vertx.deployVerticle(ApiVerticle()) }
        // this message will not be received by the consumer because too early for consumer registering "a."
        .onSuccess { vertx.eventBus().send("a.",1) }
        .onSuccess { vertx.setTimer(1000){ vertx.eventBus().send("a.",2)} }
        .onSuccess { log.info("success.") }

}