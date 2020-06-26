package coruntine

import io.vertx.core.AbstractVerticle
import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.Message
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.awaitResult
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("log")

private class ServiceVerticle : AbstractVerticle() {
    override fun start() {
        val consumer = vertx.eventBus().localConsumer<String>("a.b.")
        consumer.handler {
            log.info("Consumer received: ${it.body()}")
            val request = it
            vertx.setTimer(3000) { request.reply("this is reply") }
        }
    }
}

private class ApiVerticle : CoroutineVerticle() {
    override suspend fun start() {
        vertx.setPeriodic(1000) { log.info("periodic job.") }
        vertx.eventBus().localConsumer<Int>("a.")
            .handler {
                GlobalScope.launch {
                    log.info("Send a message and wait for a reply...")
                    val reply = awaitResult<Message<String>> { h ->
                        vertx.eventBus().request("a.b.c", "ping", h)
                    }
                    log.info("Reply received: ${reply.body()}")
                }
            }
    }
}

fun main() {
    val vertx = Vertx.vertx()
    vertx.deployVerticle(ServiceVerticle())
        .onSuccess { vertx.deployVerticle(ApiVerticle()) }
}